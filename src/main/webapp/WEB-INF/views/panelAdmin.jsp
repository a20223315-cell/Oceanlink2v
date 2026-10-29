<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecera.jspf" %>
<div class="page-heading">
<h1>
       Panel general
      </h1>
<p>
       Revise el estado general de la plataforma y los elementos de infraestructura registrados.
      </p>
</div>
<div class="cards">
<article class="summary-card">
<div class="metric-icon info">
<i class="fa-solid fa-users">
</i>
</div>
<span class="metric-label">
        Usuarios activos
       </span>
<strong><c:out value="${metricas[0][1]}"/></strong>
</article>
<article class="summary-card">
<div class="metric-icon info">
<i class="fa-solid fa-anchor">
</i>
</div>
<span class="metric-label">
        Estaciones registradas
       </span>
<strong><c:out value="${metricas[1][1]}"/></strong>
</article>
<article class="summary-card">
<div class="metric-icon ok">
<i class="fa-solid fa-network-wired">
</i>
</div>
<span class="metric-label">
        Segmentos operativos
       </span>
<strong><c:out value="${metricas[2][1]}"/></strong>
</article>
<article class="summary-card">
<div class="metric-icon danger">
<i class="fa-solid fa-triangle-exclamation">
</i>
</div>
<span class="metric-label">
        Incidencias activas
       </span>
<strong><c:out value="${metricas[3][1]}"/></strong>
</article>
</div>
<article class="panel">
<div class="panel-heading">
<div>
<h2>
         Resumen de infraestructura
        </h2>
<p>
         Estado reciente de los elementos que requieren seguimiento administrativo.
        </p>
</div>
</div>
<div class="table-container">
<table data-page-size="10" data-client-page>
<thead>
<tr>
<th>
           Elemento
          </th>
<th class="cell-center">
           Identificador
          </th>
<th class="cell-center">
           Estado
          </th>
<th class="cell-center">
           Última actualización
          </th>
</tr>
</thead>
<tbody><c:forEach var="fila" items="${infraestructura}"><tr><td><c:out value="${fila[0]}"/></td><td><c:out value="${fila[1]}"/></td><td><span class="status"><c:out value="${etiquetas[fila[2]] != null ? etiquetas[fila[2]] : fila[2]}"/></span></td><td><c:out value="${fila[3]}"/></td></tr></c:forEach></tbody>
</table>
</div>
</article>
<%@ include file="pie.jspf" %>
