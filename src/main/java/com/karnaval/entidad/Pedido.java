package com.karnaval.entidad;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "idCliente", 
			referencedColumnName = "id", 
			foreignKey = @ForeignKey(name = "fk_cliente_pedidos"), 
			nullable = false)
	private Cliente cliente;

	@Column(name = "fecha", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime fecha;

	@Enumerated(EnumType.STRING)
	private TipoComprobante tipoComprobante;

	@Enumerated(EnumType.STRING)
	private EstadoPedido estadoPedido;
}
