package pe.altoque.referencia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DistritoRepository extends JpaRepository<Distrito, String> {

    @Query("""
            select d from Distrito d join fetch d.provincia p join fetch p.departamento dep
            where lower(d.nombre) like lower(concat(:q, '%')) or lower(p.nombre) like lower(concat(:q, '%')) or d.ubigeo = :q
            order by case when lower(d.nombre) = lower(:q) then 0 else 1 end, d.nombre""")
    List<Distrito> buscar(String q, Pageable pageable);

    /** Busca por nombres (como los devuelve RENIEC/SUNAT): distrito + provincia + departamento. */
    @Query("""
            select d from Distrito d join d.provincia p join p.departamento dep
            where upper(d.nombre) = upper(:distrito) and upper(p.nombre) = upper(:provincia) and upper(dep.nombre) = upper(:departamento)""")
    List<Distrito> porNombres(String distrito, String provincia, String departamento);
}
