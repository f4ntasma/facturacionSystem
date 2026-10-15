package pe.altoque.sunat.xml;

import pe.altoque.comprobante.Calculadora;
import pe.altoque.comprobante.Comprobante;
import pe.altoque.comprobante.ComprobanteCuota;
import pe.altoque.comprobante.ComprobanteDetalle;
import pe.altoque.comprobante.TipoComprobante;
import pe.altoque.empresa.Empresa;
import pe.altoque.sunat.ComunicacionBaja;
import pe.altoque.sunat.ComunicacionBajaItem;
import pe.altoque.sunat.ResumenDiario;
import pe.altoque.sunat.ResumenDiarioItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import static pe.altoque.common.Montos.txt;

/**
 * Arma los XML UBL 2.1 que pide SUNAT: Invoice (factura/boleta), CreditNote, DebitNote,
 * SummaryDocuments (RC) y VoidedDocuments (RA). El nodo ext:ExtensionContent queda vacío para la firma.
 */
public final class UblBuilder {

    public static final String FIRMA_ID = "SignAltoque";

    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String NS_SAC = "urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private UblBuilder() {}

    // ------------------------------------------------------------------ comprobantes

    public static String comprobante(Comprobante c) {
        return switch (c.getTipo()) {
            case FACTURA, BOLETA -> documento(c, "Invoice", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2", "InvoiceLine", "InvoicedQuantity");
            case NOTA_CREDITO -> documento(c, "CreditNote", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2", "CreditNoteLine", "CreditedQuantity");
            case NOTA_DEBITO -> documento(c, "DebitNote", "urn:oasis:names:specification:ubl:schema:xsd:DebitNote-2", "DebitNoteLine", "DebitedQuantity");
        };
    }

    private static String documento(Comprobante c, String raiz, String ns, String tagLinea, String tagCantidad) {
        Empresa e = c.getEmpresa();
        String m = c.getMoneda();
        Xml x = new Xml();
        x.abrir(raiz, "xmlns", ns, "xmlns:cac", NS_CAC, "xmlns:cbc", NS_CBC, "xmlns:ds", NS_DS, "xmlns:ext", NS_EXT);
        extensiones(x);
        x.el("cbc:UBLVersionID", "2.1");
        x.el("cbc:CustomizationID", "2.0");
        x.el("cbc:ID", c.numero());
        x.el("cbc:IssueDate", c.getFechaEmision());
        x.el("cbc:IssueTime", c.getHoraEmision().format(HORA));
        if (c.getTipo() == TipoComprobante.FACTURA || c.getTipo() == TipoComprobante.BOLETA) {
            if (c.getFechaVencimiento() != null) x.el("cbc:DueDate", c.getFechaVencimiento());
            x.el("cbc:InvoiceTypeCode", c.getTipo().codigo(), "listID", c.getTipoOperacion());
        }
        x.el("cbc:Note", c.getTotalLetras(), "languageLocaleID", "1000");
        x.el("cbc:DocumentCurrencyCode", m);

        if (c.getTipo().esNota()) {
            String ref = c.getRefSerie() + "-" + c.getRefCorrelativo();
            x.abrir("cac:DiscrepancyResponse")
                    .el("cbc:ReferenceID", ref)
                    .el("cbc:ResponseCode", c.getMotivoCodigo())
                    .el("cbc:Description", c.getMotivoDescripcion())
                    .cerrar("cac:DiscrepancyResponse");
            x.abrir("cac:BillingReference").abrir("cac:InvoiceDocumentReference")
                    .el("cbc:ID", ref)
                    .el("cbc:DocumentTypeCode", c.getRefTipo().codigo())
                    .cerrar("cac:InvoiceDocumentReference").cerrar("cac:BillingReference");
        }

        firmaReferencia(x, e);
        emisor(x, c);

        x.abrir("cac:AccountingCustomerParty").abrir("cac:Party");
        x.abrir("cac:PartyIdentification").el("cbc:ID", c.getClienteNumDoc(), "schemeID", c.getClienteTipoDoc()).cerrar("cac:PartyIdentification");
        x.abrir("cac:PartyLegalEntity").el("cbc:RegistrationName", c.getClienteNombre());
        if (c.getClienteDireccion() != null && !c.getClienteDireccion().isBlank()) {
            x.abrir("cac:RegistrationAddress").abrir("cac:AddressLine").el("cbc:Line", c.getClienteDireccion())
                    .cerrar("cac:AddressLine").cerrar("cac:RegistrationAddress");
        }
        x.cerrar("cac:PartyLegalEntity").cerrar("cac:Party").cerrar("cac:AccountingCustomerParty");

        if (c.getTipo() == TipoComprobante.FACTURA) {
            formaPago(x, c);
        }

        impuestosDocumento(x, c);

        String tagTotales = c.getTipo() == TipoComprobante.NOTA_DEBITO ? "cac:RequestedMonetaryTotal" : "cac:LegalMonetaryTotal";
        x.abrir(tagTotales)
                .el("cbc:LineExtensionAmount", txt(Calculadora.valorVentaTotal(c)), "currencyID", m)
                .el("cbc:TaxInclusiveAmount", txt(c.getTotal()), "currencyID", m)
                .el("cbc:PayableAmount", txt(c.getTotal()), "currencyID", m)
                .cerrar(tagTotales);

        for (ComprobanteDetalle d : c.getDetalles()) {
            linea(x, c, d, "cac:" + tagLinea, "cbc:" + tagCantidad);
        }
        x.cerrar(raiz);
        return x.toString();
    }

    private static void formaPago(Xml x, Comprobante c) {
        String m = c.getMoneda();
        if (c.getFormaPago() == Comprobante.FormaPago.CREDITO && !c.getCuotas().isEmpty()) {
            BigDecimal pendiente = c.getCuotas().stream().map(ComprobanteCuota::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
            x.abrir("cac:PaymentTerms").el("cbc:ID", "FormaPago").el("cbc:PaymentMeansID", "Credito")
                    .el("cbc:Amount", txt(pendiente), "currencyID", m).cerrar("cac:PaymentTerms");
            for (ComprobanteCuota q : c.getCuotas()) {
                x.abrir("cac:PaymentTerms").el("cbc:ID", "FormaPago")
                        .el("cbc:PaymentMeansID", "Cuota%03d".formatted(q.getNumero()))
                        .el("cbc:Amount", txt(q.getMonto()), "currencyID", m)
                        .el("cbc:PaymentDueDate", q.getFechaVencimiento())
                        .cerrar("cac:PaymentTerms");
            }
        } else {
            x.abrir("cac:PaymentTerms").el("cbc:ID", "FormaPago").el("cbc:PaymentMeansID", "Contado").cerrar("cac:PaymentTerms");
        }
    }

    private static void impuestosDocumento(Xml x, Comprobante c) {
        String m = c.getMoneda();
        x.abrir("cac:TaxTotal").el("cbc:TaxAmount", txt(c.getTotalIgv().add(c.getTotalIcbper())), "currencyID", m);
        boolean alguno = false;
        if (c.getTotalGravado().signum() > 0) {
            subtotal(x, m, c.getTotalGravado(), c.getTotalIgv(), "1000", "IGV", "VAT");
            alguno = true;
        }
        if (c.getTotalExonerado().signum() > 0) {
            subtotal(x, m, c.getTotalExonerado(), BigDecimal.ZERO, "9997", "EXO", "VAT");
            alguno = true;
        }
        if (c.getTotalInafecto().signum() > 0) {
            subtotal(x, m, c.getTotalInafecto(), BigDecimal.ZERO, "9998", "INA", "FRE");
            alguno = true;
        }
        if (!alguno) {
            subtotal(x, m, BigDecimal.ZERO, BigDecimal.ZERO, "1000", "IGV", "VAT");
        }
        if (c.getTotalIcbper().signum() > 0) {
            x.abrir("cac:TaxSubtotal").el("cbc:TaxAmount", txt(c.getTotalIcbper()), "currencyID", m);
            esquema(x.abrir("cac:TaxCategory"), "7152", "ICBPER", "OTH").cerrar("cac:TaxCategory").cerrar("cac:TaxSubtotal");
        }
        x.cerrar("cac:TaxTotal");
    }

    private static void subtotal(Xml x, String m, BigDecimal base, BigDecimal impuesto, String id, String nombre, String tipo) {
        x.abrir("cac:TaxSubtotal")
                .el("cbc:TaxableAmount", txt(base), "currencyID", m)
                .el("cbc:TaxAmount", txt(impuesto), "currencyID", m);
        esquema(x.abrir("cac:TaxCategory"), id, nombre, tipo).cerrar("cac:TaxCategory").cerrar("cac:TaxSubtotal");
    }

    private static Xml esquema(Xml x, String id, String nombre, String tipo) {
        return x.abrir("cac:TaxScheme").el("cbc:ID", id).el("cbc:Name", nombre).el("cbc:TaxTypeCode", tipo).cerrar("cac:TaxScheme");
    }

    private static void linea(Xml x, Comprobante c, ComprobanteDetalle d, String tagLinea, String tagCantidad) {
        String m = c.getMoneda();
        x.abrir(tagLinea);
        x.el("cbc:ID", d.getItem());
        x.el(tagCantidad, d.getCantidad().stripTrailingZeros().toPlainString(), "unitCode", d.getUnidadMedida());
        x.el("cbc:LineExtensionAmount", txt(d.getValorVenta()), "currencyID", m);
        x.abrir("cac:PricingReference").abrir("cac:AlternativeConditionPrice")
                .el("cbc:PriceAmount", d.getPrecioUnitario().stripTrailingZeros().toPlainString(), "currencyID", m)
                .el("cbc:PriceTypeCode", "01")
                .cerrar("cac:AlternativeConditionPrice").cerrar("cac:PricingReference");

        x.abrir("cac:TaxTotal").el("cbc:TaxAmount", txt(d.getIgv().add(d.getIcbper())), "currencyID", m);
        x.abrir("cac:TaxSubtotal")
                .el("cbc:TaxableAmount", txt(d.getValorVenta()), "currencyID", m)
                .el("cbc:TaxAmount", txt(d.getIgv()), "currencyID", m);
        x.abrir("cac:TaxCategory");
        switch (d.getTipoAfectacion()) {
            case Calculadora.GRAVADO -> {
                x.el("cbc:Percent", c.getIgvPorcentaje().stripTrailingZeros().toPlainString());
                x.el("cbc:TaxExemptionReasonCode", d.getTipoAfectacion());
                esquema(x, "1000", "IGV", "VAT");
            }
            case Calculadora.EXONERADO -> {
                x.el("cbc:Percent", c.getIgvPorcentaje().stripTrailingZeros().toPlainString());
                x.el("cbc:TaxExemptionReasonCode", d.getTipoAfectacion());
                esquema(x, "9997", "EXO", "VAT");
            }
            default -> {
                x.el("cbc:Percent", "0");
                x.el("cbc:TaxExemptionReasonCode", d.getTipoAfectacion());
                esquema(x, "9998", "INA", "FRE");
            }
        }
        x.cerrar("cac:TaxCategory").cerrar("cac:TaxSubtotal");
        if (d.getIcbper().signum() > 0) {
            BigDecimal bolsas = d.getIcbper().divide(Calculadora.ICBPER_UNITARIO, 0, java.math.RoundingMode.HALF_UP);
            x.abrir("cac:TaxSubtotal")
                    .el("cbc:TaxAmount", txt(d.getIcbper()), "currencyID", m)
                    .el("cbc:BaseUnitMeasure", bolsas.toPlainString(), "unitCode", d.getUnidadMedida());
            x.abrir("cac:TaxCategory").el("cbc:PerUnitAmount", txt(Calculadora.ICBPER_UNITARIO), "currencyID", m);
            esquema(x, "7152", "ICBPER", "OTH").cerrar("cac:TaxCategory").cerrar("cac:TaxSubtotal");
        }
        x.cerrar("cac:TaxTotal");

        x.abrir("cac:Item").el("cbc:Description", d.getDescripcion());
        if (d.getCodigo() != null && !d.getCodigo().isBlank()) {
            x.abrir("cac:SellersItemIdentification").el("cbc:ID", d.getCodigo()).cerrar("cac:SellersItemIdentification");
        }
        x.cerrar("cac:Item");
        x.abrir("cac:Price").el("cbc:PriceAmount", d.getValorUnitario().stripTrailingZeros().toPlainString(), "currencyID", m).cerrar("cac:Price");
        x.cerrar(tagLinea);
    }

    // ------------------------------------------------------------------ resumen diario y bajas

    public static String resumenDiario(ResumenDiario r) {
        Empresa e = r.getEmpresa();
        Xml x = new Xml();
        x.abrir("SummaryDocuments", "xmlns", "urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1",
                "xmlns:cac", NS_CAC, "xmlns:cbc", NS_CBC, "xmlns:ds", NS_DS, "xmlns:ext", NS_EXT, "xmlns:sac", NS_SAC);
        extensiones(x);
        x.el("cbc:UBLVersionID", "2.0").el("cbc:CustomizationID", "1.1");
        x.el("cbc:ID", r.getIdentificador());
        x.el("cbc:ReferenceDate", r.getFechaReferencia());
        x.el("cbc:IssueDate", r.getFechaGeneracion());
        firmaReferencia(x, e);
        emisorLote(x, e);
        for (ResumenDiarioItem item : r.getItems()) {
            Comprobante c = item.getComprobante();
            String m = c.getMoneda();
            x.abrir("sac:SummaryDocumentsLine");
            x.el("cbc:LineID", item.getLinea());
            x.el("cbc:DocumentTypeCode", c.getTipo().codigo());
            x.el("cbc:ID", c.numero());
            x.abrir("cac:AccountingCustomerParty")
                    .el("cbc:CustomerAssignedAccountID", c.getClienteNumDoc())
                    .el("cbc:AdditionalAccountID", c.getClienteTipoDoc())
                    .cerrar("cac:AccountingCustomerParty");
            if (c.getTipo().esNota()) {
                x.abrir("cac:BillingReference").abrir("cac:InvoiceDocumentReference")
                        .el("cbc:ID", c.getRefSerie() + "-" + c.getRefCorrelativo())
                        .el("cbc:DocumentTypeCode", c.getRefTipo().codigo())
                        .cerrar("cac:InvoiceDocumentReference").cerrar("cac:BillingReference");
            }
            x.abrir("cac:Status").el("cbc:ConditionCode", item.getCondicion()).cerrar("cac:Status");
            x.el("sac:TotalAmount", txt(c.getTotal()), "currencyID", m);
            pagoResumen(x, m, c.getTotalGravado(), "01");
            pagoResumen(x, m, c.getTotalExonerado(), "02");
            pagoResumen(x, m, c.getTotalInafecto(), "03");
            x.abrir("cac:TaxTotal").el("cbc:TaxAmount", txt(c.getTotalIgv()), "currencyID", m);
            x.abrir("cac:TaxSubtotal").el("cbc:TaxAmount", txt(c.getTotalIgv()), "currencyID", m);
            esquema(x.abrir("cac:TaxCategory"), "1000", "IGV", "VAT").cerrar("cac:TaxCategory");
            x.cerrar("cac:TaxSubtotal").cerrar("cac:TaxTotal");
            if (c.getTotalIcbper().signum() > 0) {
                x.abrir("cac:TaxTotal").el("cbc:TaxAmount", txt(c.getTotalIcbper()), "currencyID", m);
                x.abrir("cac:TaxSubtotal").el("cbc:TaxAmount", txt(c.getTotalIcbper()), "currencyID", m);
                esquema(x.abrir("cac:TaxCategory"), "7152", "ICBPER", "OTH").cerrar("cac:TaxCategory");
                x.cerrar("cac:TaxSubtotal").cerrar("cac:TaxTotal");
            }
            x.cerrar("sac:SummaryDocumentsLine");
        }
        x.cerrar("SummaryDocuments");
        return x.toString();
    }

    private static void pagoResumen(Xml x, String m, BigDecimal monto, String instruccion) {
        if (monto.signum() <= 0) return;
        x.abrir("sac:BillingPayment").el("cbc:PaidAmount", txt(monto), "currencyID", m).el("cbc:InstructionID", instruccion).cerrar("sac:BillingPayment");
    }

    public static String comunicacionBaja(ComunicacionBaja b) {
        Empresa e = b.getEmpresa();
        Xml x = new Xml();
        x.abrir("VoidedDocuments", "xmlns", "urn:sunat:names:specification:ubl:peru:schema:xsd:VoidedDocuments-1",
                "xmlns:cac", NS_CAC, "xmlns:cbc", NS_CBC, "xmlns:ds", NS_DS, "xmlns:ext", NS_EXT, "xmlns:sac", NS_SAC);
        extensiones(x);
        x.el("cbc:UBLVersionID", "2.0").el("cbc:CustomizationID", "1.0");
        x.el("cbc:ID", b.getIdentificador());
        x.el("cbc:ReferenceDate", b.getFechaReferencia());
        x.el("cbc:IssueDate", b.getFechaGeneracion());
        firmaReferencia(x, e);
        emisorLote(x, e);
        for (ComunicacionBajaItem item : b.getItems()) {
            Comprobante c = item.getComprobante();
            x.abrir("sac:VoidedDocumentsLine")
                    .el("cbc:LineID", item.getLinea())
                    .el("cbc:DocumentTypeCode", c.getTipo().codigo())
                    .el("sac:DocumentSerialID", c.getSerie())
                    .el("sac:DocumentNumberID", c.getCorrelativo())
                    .el("sac:VoidReasonDescription", item.getMotivo())
                    .cerrar("sac:VoidedDocumentsLine");
        }
        x.cerrar("VoidedDocuments");
        return x.toString();
    }

    // ------------------------------------------------------------------ partes comunes

    private static void extensiones(Xml x) {
        x.abrir("ext:UBLExtensions").abrir("ext:UBLExtension").vacio("ext:ExtensionContent").cerrar("ext:UBLExtension").cerrar("ext:UBLExtensions");
    }

    private static void firmaReferencia(Xml x, Empresa e) {
        x.abrir("cac:Signature")
                .el("cbc:ID", e.getRuc())
                .abrir("cac:SignatoryParty")
                .abrir("cac:PartyIdentification").el("cbc:ID", e.getRuc()).cerrar("cac:PartyIdentification")
                .abrir("cac:PartyName").el("cbc:Name", e.getRazonSocial()).cerrar("cac:PartyName")
                .cerrar("cac:SignatoryParty")
                .abrir("cac:DigitalSignatureAttachment").abrir("cac:ExternalReference")
                .el("cbc:URI", "#" + FIRMA_ID)
                .cerrar("cac:ExternalReference").cerrar("cac:DigitalSignatureAttachment")
                .cerrar("cac:Signature");
    }

    private static void emisor(Xml x, Comprobante c) {
        Empresa e = c.getEmpresa();
        x.abrir("cac:AccountingSupplierParty").abrir("cac:Party");
        x.abrir("cac:PartyIdentification").el("cbc:ID", e.getRuc(), "schemeID", "6").cerrar("cac:PartyIdentification");
        if (e.getNombreComercial() != null && !e.getNombreComercial().isBlank()) {
            x.abrir("cac:PartyName").el("cbc:Name", e.getNombreComercial()).cerrar("cac:PartyName");
        }
        x.abrir("cac:PartyLegalEntity").el("cbc:RegistrationName", e.getRazonSocial());
        x.abrir("cac:RegistrationAddress")
                .el("cbc:ID", e.getUbigeo())
                .el("cbc:AddressTypeCode", c.getEstablecimiento().getCodigoSunat())
                .el("cbc:CitySubdivisionName", e.getUrbanizacion() == null || e.getUrbanizacion().isBlank() ? "-" : e.getUrbanizacion())
                .el("cbc:CityName", e.getProvincia())
                .el("cbc:CountrySubentity", e.getDepartamento())
                .el("cbc:District", e.getDistrito())
                .abrir("cac:AddressLine").el("cbc:Line", e.getDireccionFiscal()).cerrar("cac:AddressLine")
                .abrir("cac:Country").el("cbc:IdentificationCode", "PE").cerrar("cac:Country")
                .cerrar("cac:RegistrationAddress");
        x.cerrar("cac:PartyLegalEntity").cerrar("cac:Party").cerrar("cac:AccountingSupplierParty");
    }

    private static void emisorLote(Xml x, Empresa e) {
        x.abrir("cac:AccountingSupplierParty")
                .el("cbc:CustomerAssignedAccountID", e.getRuc())
                .el("cbc:AdditionalAccountID", "6")
                .abrir("cac:Party").abrir("cac:PartyLegalEntity").el("cbc:RegistrationName", e.getRazonSocial())
                .cerrar("cac:PartyLegalEntity").cerrar("cac:Party")
                .cerrar("cac:AccountingSupplierParty");
    }
}
