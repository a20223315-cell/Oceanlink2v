<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecera.jspf" %>
<div class="page-heading">
<h1>
       Capacidad por segmento
      </h1>
<p>
       Identifique segmentos con poca disponibilidad y priorice las solicitudes de capacidad.
      </p>
</div>
<article class="panel">
<div class="panel-heading">
<div>
<h2>
         Disponibilidad por segmento
        </h2>
<p>
         Compare capacidad total, utilizada, reservada y disponible.
        </p>
</div>
<a class="secondary-button" href="cp_capacity_history.html"><i class="fa-solid fa-clock-rotate-left"></i> Historial de utilización</a>
</div>
<div class="filter-panel">
<div class="filter-grid" role="search">
<div class="filter-field">
<label for="ol-control-0">Filtrar por segmento</label>
<input placeholder="Ej. SEG-101 o Lurín - Valparaíso" type="search" id="ol-control-0">
</div>
<div class="filter-field">
<label for="ol-control-1">Filtrar por utilización</label>
<select id="ol-control-1">
<option selected="">
           Todos
          </option>
<option>
           Normal (&lt;80%)
          </option>
<option>
           Alta (80%-95%)
          </option>
<option>
           Crítica (&gt;95%)
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
<th>
           Segmento
          </th>
<th class="cell-center">Total (Tbps)</th>
<th class="cell-center">Utilizada (Tbps)</th>
<th class="cell-center">Reservada (Tbps)</th>
<th class="cell-center">Disponible (Tbps)</th>
<th class="cell-center">Utilización (%)</th>
<th class="action-cell">
           Acción
          </th>
</tr>
</thead><tbody><c:forEach var="fila" items="${tablas[0]}"><tr><c:forEach var="celda" items="${fila}" varStatus="col"><td><c:choose><c:when test="${fn:startsWith(celda,'LINK:')}"><a class="icon-action" title="Ver detalle" aria-label="Ver detalle" href="${fn:substringAfter(celda,'LINK:')}"><i class="fa-regular fa-eye"></i></a></c:when><c:otherwise><c:out value="${celda}"/></c:otherwise></c:choose></td></c:forEach></tr></c:forEach></tbody></table></div>
</article>
<%@ include file="pie.jspf" %>
