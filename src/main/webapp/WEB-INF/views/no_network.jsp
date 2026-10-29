<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecera.jspf" %>
<div class="page-heading">
<h1>
       Estado de la red
      </h1>
<p>
       Supervise la condición operativa de los segmentos y revise los eventos que requieren atención.
      </p>
</div>
<div class="cards">
<article class="summary-card">
<div class="metric-icon ok">
<i class="fa-solid fa-circle-check">
</i>
</div>
<span class="metric-label">
        Operativos
       </span>
<strong>${estadoRed[0]}</strong>
</article>
<article class="summary-card">
<div class="metric-icon warning">
<i class="fa-solid fa-triangle-exclamation">
</i>
</div>
<span class="metric-label">
        Degradados
       </span>
<strong>${estadoRed[1]}</strong>
</article>
<article class="summary-card">
<div class="metric-icon info">
<i class="fa-solid fa-screwdriver-wrench">
</i>
</div>
<span class="metric-label">
        En mantenimiento
       </span>
<strong>${estadoRed[2]}</strong>
</article>
<article class="summary-card">
<div class="metric-icon danger">
<i class="fa-solid fa-circle-xmark">
</i>
</div>
<span class="metric-label">
        Fuera de servicio
       </span>
<strong>${estadoRed[3]}</strong>
</article>
</div>
<article class="panel">
<div class="panel-heading">
<div>
<h2>
         Estado actual de los segmentos
        </h2>
<p>
         Revise primero los segmentos degradados o en mantenimiento.
        </p>
</div>
</div>
<div class="filter-panel">
<div class="filter-grid" role="search">
<div class="filter-field">
<label for="ol-control-0">Filtrar por segmento</label>
<input placeholder="Ej. SEG-102" type="search" id="ol-control-0">
</div>
<div class="filter-field">
<label for="ol-control-1">Filtrar por estado actual</label>
<select id="ol-control-1">
<option selected="">
           Todos
          </option>
<option>
           Operativo
          </option>
<option>
           Degradado
          </option>
<option>
           En mantenimiento
          </option>
<option>
           Fuera de servicio
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
<div class="table-container"><table data-client-page><thead>
<tr>
<th class="cell-center">
           Segmento
          </th>
<th>
           Ruta
          </th>
<th class="cell-center">Utilización (%)</th>
<th class="cell-center">
           Estado actual
          </th>
<th class="action-cell">
           Acción
          </th>
</tr>
</thead><tbody><c:forEach var="fila" items="${tablas[0]}"><tr><c:forEach var="celda" items="${fila}" varStatus="col"><td><c:choose><c:when test="${fn:startsWith(celda,'LINK:')}"><a class="icon-action" title="Ver detalle" aria-label="Ver detalle" href="${fn:substringAfter(celda,'LINK:')}"><i class="fa-regular fa-eye"></i></a></c:when><c:otherwise><c:out value="${celda}"/></c:otherwise></c:choose></td></c:forEach></tr></c:forEach></tbody></table></div>
</article>
<%@ include file="pie.jspf" %>
