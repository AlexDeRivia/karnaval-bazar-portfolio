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

@Entity
@IdClass(PedidoDetalleID.class)
@Table(name = "pedido_detalles")
@Getter
@Setter
public class PedidoDetalle {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idPedido",
			referencedColumnName = "id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_pedidos_pedido_detalle"))
	private Pedido pedido;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idProducto",
			referencedColumnName = "id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_productos_pedido_detalle"))
	private Producto producto;

	private Integer cantidad;
	private Double precioUnitario;
}
