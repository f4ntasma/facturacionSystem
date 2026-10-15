import { Injectable, inject, signal } from '@angular/core';
import { Api } from './api.service';
import { Catalogos } from './modelos';

const VACIO: Catalogos = { documentos: {}, motivosNotaCredito: {}, motivosNotaDebito: {}, unidades: {}, afectaciones: {}, detracciones: [], mediosPago: [] };

@Injectable({ providedIn: 'root' })
export class CatalogosService {
  private api = inject(Api);
  readonly datos = signal<Catalogos>(VACIO);
  private cargado?: Promise<void>;

  cargar(): Promise<void> {
    this.cargado ??= this.api.get<Catalogos>('/public/catalogos').then((c) => this.datos.set(c)).catch(() => {
      this.cargado = undefined;
    });
    return this.cargado;
  }

  lista(nombre: 'documentos' | 'motivosNotaCredito' | 'motivosNotaDebito' | 'unidades' | 'afectaciones'): { codigo: string; nombre: string }[] {
    return Object.entries(this.datos()[nombre]).map(([codigo, n]) => ({ codigo, nombre: n }));
  }

  nombreDoc(codigo: string): string {
    if (codigo === '0') return 'Sin doc.';
    return this.datos().documentos[codigo] ?? codigo;
  }
}
