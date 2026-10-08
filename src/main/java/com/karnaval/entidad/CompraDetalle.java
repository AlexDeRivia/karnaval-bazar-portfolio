package com.karnaval.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@IdClass(CompraDetalleID.class)
@Table(name = "compra_detalles")
@Getter
@Setter
public class CompraDetalle {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idCompra",
			referencedColumnName = "id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_compras_compra_detalle"))
	private Compra compra;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idProducto",
			referencedColumnName = "id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_productos_compra_detalle"))
	@NotNull(message = "Selecciona un producto")
	private Producto producto;

	@Min(value = 1, message = "La cantidad debe ser positiva")
	@NotNull
	private Integer cantidad;
	@DecimalMin(value = "0.01", message = "El precio debe ser positivo")
	@NotNull
	private Double precioUnitario;
}
