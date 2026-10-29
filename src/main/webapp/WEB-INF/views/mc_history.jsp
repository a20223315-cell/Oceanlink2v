<%@ page pageEncoding="UTF-8" %><%@ include file="cabecera.jspf" %>
<div class="page-heading">
<h1>
       Historial de mantenimiento
      </h1>
<p>Consulte las actividades finalizadas y sus comentarios para mantener trazabilidad de la infraestructura.</p>
</div>
<article class="panel">
<div class="panel-heading">
<div>
<h2>
         Mantenimientos finalizados
        </h2>
<p>Consulte las intervenciones terminadas y el comentario registrado.</p>
</div>
</div>
<div class="filter-panel">
<div class="filter-grid filter-grid-compact" role="search"><div class="filter-field"><label for="ol-control-0">Filtrar por código</label><input placeholder="Ej. MNT-003" type="search" id="ol-control-0"></div>
<div class="filter-field">
<label for="ol-control-1">Filtrar por fecha desde</label>
<input type="date"  id="ol-control-1">
</div>
<div class="filter-field">
<label for="ol-control-2">Filtrar por fecha hasta</label>
<input type="date"  id="ol-control-2">
</div>
<div class="filter-field">
<label for="ol-control-3">Filtrar por elemento</label>
<input placeholder="Estación o segmento" type="search" id="ol-control-3">
</div>
<div class="filter-field">
<label for="ol-control-4">Filtrar por tipo</label>
<select id="ol-control-4">
<option selected="">
           Todos
          </option>
<option>
           Preventivo
          </option>
<option>
           Correctivo
          </option>
</select>
</div>
<div class="filter-actions">
<button class="primary-button filter-submit" type="button">
<i class="fa-solid fa-filter">
</i>
          Filtrar
         </button>
</div>
</div>
</div>
<div class="table-container">
<table data-page-size="10" data-client-page>
<thead>
<tr>
<th class="cell-center">
           Código
          </th>
<th class="cell-center">
           Elemento
          </th>
<th class="cell-center">
           Tipo
          </th>
<th class="cell-center">
           Fecha
          </th>
<th>Comentario</th>
<th class="action-cell">Acción</th></tr>
</thead>
<tbody><c:forEach var="fila" items="${filas}"><tr><c:forEach var="celda" items="${fila}"><td><c:choose><c:when test="${fn:startsWith(celda,'LINK:')}"><a class="icon-action" title="Ver detalle" aria-label="Ver detalle" href="${fn:substringAfter(celda,'LINK:')}"><i class="fa-regular fa-eye" aria-hidden="true"></i></a></c:when><c:otherwise><c:out value="${etiquetas[celda] != null ? etiquetas[celda] : celda}"/></c:otherwise></c:choose></td></c:forEach></tr></c:forEach></tbody>
</table>
</div>
</article>
<%@ include file="pie.jspf" %>
