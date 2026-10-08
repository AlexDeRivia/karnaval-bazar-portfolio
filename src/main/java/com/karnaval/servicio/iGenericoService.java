package com.karnaval.servicio;

import java.util.List;

public interface iGenericoService<Entidad, ID> {

	public Entidad agregar(Entidad entidad);

	public List<Entidad> listarTodos();

	public Entidad buscar(ID id);

	public Entidad actualizar(Entidad entidad);

	public void eliminar(ID id);
}
