document.addEventListener('DOMContentLoaded',()=>{
 'use strict';
 const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
 const body=document.body,collapse=$('.sidebar-collapse-toggle'),mobile=$('.sidebar-mobile-toggle'),overlay=$('.sidebar-overlay');
 const desktop=matchMedia('(min-width:901px)');
 const sync=()=>{
   const expanded=desktop.matches?!body.classList.contains('sidebar-collapsed'):body.classList.contains('sidebar-mobile-open');
   collapse?.setAttribute('aria-expanded',String(expanded));
   collapse?.setAttribute('aria-label',desktop.matches?(expanded?'Contraer menú lateral':'Expandir menú lateral'):'Cerrar menú lateral');
   const icon=collapse?.querySelector('i');
   icon?.classList.toggle('fa-angles-left',expanded);
   icon?.classList.toggle('fa-angles-right',!expanded);
   mobile?.setAttribute('aria-expanded',String(body.classList.contains('sidebar-mobile-open')));
   mobile?.setAttribute('aria-label',body.classList.contains('sidebar-mobile-open')?'Cerrar menú lateral':'Abrir menú lateral');
 };
 collapse?.addEventListener('click',()=>{
   if(!desktop.matches){body.classList.remove('sidebar-mobile-open');sync();return;}
   body.classList.toggle('sidebar-collapsed');
   try{localStorage.setItem('oceanlink.final.sidebarCollapsed',body.classList.contains('sidebar-collapsed')?'1':'0');}catch(error){}
   sync();
 });
 mobile?.addEventListener('click',()=>{body.classList.toggle('sidebar-mobile-open');sync();});
 overlay?.addEventListener('click',()=>{body.classList.remove('sidebar-mobile-open');sync();});
 desktop.addEventListener('change',()=>{body.classList.remove('sidebar-mobile-open');sync();});
 document.addEventListener('keydown',event=>{if(event.key==='Escape'){body.classList.remove('sidebar-mobile-open');sync();}});
 sync();
 $$('[data-submit-on-change]').forEach(s=>s.addEventListener('change',()=>s.form.requestSubmit()));
 const confirm=document.createElement('div');confirm.className='modal app-confirm-modal';confirm.innerHTML='<button class="modal-background" type="button" aria-label="Cerrar ventana"></button><div class="modal-box confirm-box" role="dialog" aria-modal="true" aria-labelledby="confirm-title"><div class="confirm-icon"><i class="fa-solid fa-circle-question" aria-hidden="true"></i></div><div class="confirm-copy"><h2 id="confirm-title">Confirmar operación</h2><p></p></div><div class="confirm-actions"><button type="button" class="danger-button" data-cancel>Cancelar</button><button type="button" class="primary-button" data-accept>Confirmar</button></div></div>';body.append(confirm);
 let confirmation=null,previous=null,hadModal=false;
 const finish=accepted=>{if(!confirmation)return;const resolve=confirmation;confirmation=null;confirm.classList.remove('is-open');if(!hadModal)body.classList.remove('modal-open');previous?.focus({preventScroll:true});resolve(accepted);};
 window.OceanLinkConfirm=(message,title='Confirmar operación')=>new Promise(resolve=>{if(confirmation){resolve(false);return;}confirmation=resolve;previous=document.activeElement;hadModal=body.classList.contains('modal-open');$('h2',confirm).textContent=title;$('p',confirm).textContent=message;confirm.classList.add('is-open');body.classList.add('modal-open');$('[data-cancel]',confirm).focus({preventScroll:true});});
 $('[data-accept]',confirm).addEventListener('click',()=>finish(true));$('[data-cancel]',confirm).addEventListener('click',()=>finish(false));$('.modal-background',confirm).addEventListener('click',()=>finish(false));
 document.addEventListener('keydown',e=>{if(!confirmation)return;if(e.key==='Escape'){e.preventDefault();e.stopImmediatePropagation();finish(false);}if(e.key==='Tab'){const items=$$('button',confirm).filter(x=>!x.classList.contains('modal-background'));if(e.shiftKey&&document.activeElement===items[0]){e.preventDefault();items.at(-1).focus();}else if(!e.shiftKey&&document.activeElement===items.at(-1)){e.preventDefault();items[0].focus();}}},true);
 document.addEventListener('submit',async e=>{
 const form=e.target;if(!form.matches('form[data-confirm]')||form.matches('[data-workflow]'))return;e.preventDefault();if(form.dataset.confirming||form.dataset.submitting)return;form.dataset.confirming='true';
 let message=form.dataset.confirm,title='Confirmar operación';
 if(new URL(form.getAttribute('action')||location.href,location.href).pathname.endsWith('/UsuarioServlet')&&form.elements.action?.value==='crear'){const name=[form.elements.apellidos?.value.trim(),form.elements.nombres?.value.trim()].filter(Boolean).join(', ');title='Registrar usuario';message='¿Crear la cuenta de '+name+'? Quedará pendiente de activación.';}
 let ask=window.OceanLinkConfirm;try{if(parent!==window&&parent.location.origin===location.origin&&parent.OceanLinkConfirm)ask=parent.OceanLinkConfirm;}catch{}
 const accepted=await ask(message,title);delete form.dataset.confirming;if(!accepted)return;
 const f=form;f.dataset.submitting='true';const toggle=new URL(f.getAttribute('action')||location.href,location.href).pathname.endsWith('/EstadoUsuarioServlet');
 if(!toggle){f.querySelectorAll('button[type=submit]').forEach(b=>b.disabled=true);HTMLFormElement.prototype.submit.call(f);return;}
 const buttons=[...f.querySelectorAll('button')];buttons.forEach(b=>b.disabled=true);
 try{const response=await fetch(f.action,{method:'POST',headers:{'X-Requested-With':'XMLHttpRequest'},body:new URLSearchParams(new FormData(f))});const result=await response.text();if(!response.ok)throw Error(result||'No se pudo actualizar la cuenta.');await window.OceanLinkUpdateList(location.href);window.OceanLinkToast?.(result,'success');}catch(error){window.OceanLinkToast?.(error.message,'error');}finally{delete f.dataset.submitting;buttons.forEach(b=>b.disabled=false);}
 });
 const origen=$('#origenId'),destino=$('#destinoId'),ruta=$('#ruta');let routeRequest=0;
 const cargarRutas=async()=>{const generation=++routeRequest;if(!ruta)return;if(!origen?.value||!destino?.value||origen.value===destino.value){ruta.replaceChildren(new Option(origen?.value===destino?.value?'Seleccione estaciones diferentes':'Seleccione origen y destino',''));ruta.disabled=false;window.OceanLinkControls.refreshSelect(ruta);return;}ruta.disabled=true;window.OceanLinkControls.refreshSelect(ruta);try{const response=await fetch(`RutasServlet?origen=${encodeURIComponent(origen.value)}&destino=${encodeURIComponent(destino.value)}`);if(!response.ok)throw Error();const rutas=await response.json();if(generation!==routeRequest)return;ruta.replaceChildren(new Option(rutas.length?'Seleccione una ruta':'No existe una ruta entre estas estaciones',''));for(const r of rutas)ruta.add(new Option(r.label,r.value));$('[data-route-status]').textContent=rutas.length?'Seleccione la ruta que conecta las estaciones.':'Revise las estaciones y los segmentos registrados.';}catch(_){$('[data-route-status]').textContent='No se pudieron consultar las rutas. Inténtelo nuevamente.';}finally{if(generation===routeRequest){ruta.disabled=false;window.OceanLinkControls.refreshSelect(ruta);}}};
 if(origen?.tagName==='SELECT'&&destino?.tagName==='SELECT'){origen.addEventListener('change',cargarRutas);destino.addEventListener('change',cargarRutas);}
 const impacto=$('[data-impacto]');impacto?.addEventListener('click',async()=>{const estacion=$('#estacionId').value,segmento=$('#segmentoId').value,tbody=$('[data-impacto-tabla]');if(Boolean(estacion)===Boolean(segmento)){impacto.textContent='Seleccione una estación o un segmento';return;}impacto.disabled=true;try{const response=await fetch(`ImpactoServlet?estacionId=${encodeURIComponent(estacion)}&segmentoId=${encodeURIComponent(segmento)}`);if(!response.ok)throw Error();const rows=await response.json();tbody.replaceChildren();for(const data of rows){const row=tbody.insertRow();for(const value of data)row.insertCell().textContent=value;}if(!rows.length){const c=tbody.insertRow().insertCell();c.colSpan=4;c.textContent='Sin servicios activos afectados.';}tbody.closest('table')._refresh?.();impacto.textContent='Consultar impacto';}catch(_){impacto.textContent='No se pudo consultar. Reintentar';}finally{impacto.disabled=false;}});
 $$('table[data-client-page]').forEach(table=>{
  const pagination=document.createElement('div');pagination.className='table-pagination';pagination.innerHTML='<div class="pagination-size"><label>Mostrar</label><select aria-label="Registros por página"><option>10</option><option>25</option><option>50</option></select><span>registros</span></div><div class="pagination-info" aria-live="polite"></div><div class="pagination-controls"><button type="button" class="pagination-button" aria-label="Página anterior">‹</button><span class="pagination-button active" aria-current="page"></span><button type="button" class="pagination-button" aria-label="Página siguiente">›</button></div>';table.closest('.table-container').after(pagination);
  const emptyRow=table.tBodies[0].insertRow();emptyRow.hidden=true;const emptyCell=emptyRow.insertCell();emptyCell.colSpan=table.querySelectorAll('thead th').length;emptyCell.className='crud-empty';emptyCell.textContent='No se encontraron registros para los filtros seleccionados.';const size=$('select',pagination),info=$('.pagination-info',pagination),buttons=$$('button',pagination);let page=1;
  const render=()=>{const q=($('[data-table-search]')?.value||'').toLocaleLowerCase('es'),util=$('[data-utilizacion]')?.value||'';const all=[...table.tBodies[0].rows],rows=all.filter(r=>r.cells.length>1&&r.dataset.portalMatch!=='false'&&r.textContent.toLocaleLowerCase('es').includes(q)&&(!util||(r.dataset.utilizacion||r.cells[r.cells.length-1].textContent)===util));const pages=Math.max(1,Math.ceil(rows.length/Number(size.value)));page=Math.min(page,pages);all.forEach(r=>r.hidden=true);emptyRow.hidden=rows.length>0;rows.slice((page-1)*Number(size.value),page*Number(size.value)).forEach(r=>r.hidden=false);info.textContent=rows.length?`Mostrando ${(page-1)*Number(size.value)+1}–${Math.min(page*Number(size.value),rows.length)} de ${rows.length}`:'Sin registros para mostrar';$('.pagination-controls span',pagination).textContent=String(page);$('.pagination-controls span',pagination).setAttribute('aria-label',`Página ${page} de ${pages}`);buttons[0].disabled=page===1;buttons[1].disabled=page===pages;};
  buttons[0].addEventListener('click',()=>{page--;render();});buttons[1].addEventListener('click',()=>{page++;render();});size.addEventListener('change',()=>{page=1;render();});$('[data-table-search]')?.addEventListener('input',()=>{page=1;render();});$('[data-utilizacion]')?.addEventListener('change',()=>{page=1;render();});table._refresh=render;window.OceanLinkControls.enhanceSelect(size);render();
 });
});
