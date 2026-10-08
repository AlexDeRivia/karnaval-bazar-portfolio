package com.karnaval.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;
import jakarta.persistence.LockModeType;

import com.karnaval.entidad.Producto;

public interface ProductoRepository
		extends JpaRepository<Producto, Long> {
	@Modifying(flushAutomatically = true)
	@Query("update Producto p set p.stock = p.stock - :quantity where p.id = :id "
			+ "and p.stock >= :quantity and p.precio = :price")
	int reserve(@Param("id") Long id, @Param("quantity") int quantity,
			@Param("price") BigDecimal price);

	@Modifying(flushAutomatically = true)
	@Query("update Producto p set p.stock = p.stock + :quantity where p.id = :id")
	int release(@Param("id") Long id, @Param("quantity") int quantity);

	@Modifying(flushAutomatically = true)
	@Query("update Producto p set p.nombre = :nombre, p.precio = :precio, p.stock = :stock, "
			+ "p.descripcion = :descripcion, p.ubicacionAlmacen = :ubicacion, p.foto = :foto, "
			+ "p.categoria = :categoria where p.id = :id and p.stock = :expectedStock")
	int updateIfStockMatches(@Param("id") Long id, @Param("expectedStock") int expectedStock,
			@Param("nombre") String nombre, @Param("precio") BigDecimal precio,
			@Param("stock") int stock, @Param("descripcion") String descripcion,
			@Param("ubicacion") String ubicacion, @Param("foto") String foto,
			@Param("categoria") String categoria);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from Producto p where p.id = :id")
	Optional<Producto> findForUpdate(@Param("id") Long id);

}
