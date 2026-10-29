document.addEventListener('DOMContentLoaded',()=>{
 'use strict';
 let listRequest=0;
 const updateList=async url=>{
   const generation=++listRequest,table=document.querySelector('table[data-server-sort]');
   if(!table)return;
   table.setAttribute('aria-busy','true');
   try{
     const response=await fetch(url,{credentials:'same-origin'});
     if(!response.ok)throw Error('No se pudieron cargar los resultados.');
     const doc=new DOMParser().parseFromString(await response.text(),'text/html');
     const next=doc.querySelector('table[data-server-sort] tbody');
     if(!next)throw Error('No se pudieron cargar los resultados. Recarga la página.');
     if(generation!==listRequest)return;
     const x=scrollX,y=scrollY;
     document.querySelector('[data-list-error]')?.remove();
     table.tBodies[0].replaceChildren(...next.childNodes);
     table.querySelectorAll('.status').forEach(el=>{el.classList.remove('ok','info','warning','danger');el.classList.add(stateClass(el.textContent));});
     const pagination=document.querySelector('.table-pagination'),incoming=doc.querySelector('.table-pagination');
     if(pagination&&incoming){
       pagination.replaceWith(incoming);
       incoming.querySelectorAll('select').forEach(select=>{
         window.OceanLinkControls?.enhanceSelect(select);
         if(select.hasAttribute('data-submit-on-change'))select.addEventListener('change',()=>select.form.requestSubmit());
       });
     }
     history.replaceState(null,'',url);
     document.querySelectorAll('.filter-panel input[type=hidden]').forEach(input=>{
       const value=new URL(url,location.href).searchParams.get(input.name);
       input.value=value??'';
     });
     document.dispatchEvent(new CustomEvent('oceanlink:list-updated'));
     window.scrollTo({left:x,top:y,behavior:'instant'});
   }catch(error){
     if(generation===listRequest){
       let message=document.querySelector('[data-list-error]');
       if(!message){message=document.createElement('p');message.dataset.listError='';message.setAttribute('role','alert');table.closest('.table-container').before(message);}
       message.textContent=error.message;
     }
   }finally{if(generation===listRequest)table.removeAttribute('aria-busy');}
 };
 window.OceanLinkUpdateList=updateList;
 document.addEventListener('submit',event=>{
   const form=event.target;
   if(!form.matches('form.filter-panel,form.pagination-size')||!document.querySelector('table[data-server-sort]'))return;
   event.preventDefault();
   const url=new URL(form.action,location.href);url.search=new URLSearchParams(new FormData(form)).toString();updateList(url);
 });
 document.addEventListener('click',event=>{
   const link=event.target.closest('.filter-panel a[href],.pagination-controls a[href]');
   if(!link||event.defaultPrevented||event.ctrlKey||event.metaKey||event.shiftKey||event.altKey||event.button!==0)return;
   const url=new URL(link.href);
   if(url.origin===location.origin&&url.pathname===location.pathname&&document.querySelector('table[data-server-sort]')){
     event.preventDefault();
     if(link.closest('.filter-panel')){const form=link.closest('form');form?.querySelectorAll('input:not([type=hidden]),select').forEach(control=>{control.value='';if(control.tagName==='SELECT')window.OceanLinkControls?.refreshSelect(control);});}
     updateList(url);
   }
 });
 const params=new URLSearchParams(location.search);
 const normalize=s=>s.trim().toLocaleLowerCase('es').normalize('NFD').replace(/[\u0300-\u036f]/g,'');
 const stateClass=text=>{
   const t=normalize(text);
   if(['activo','activa','operativo','operativa','normal','restaurada','cerrada','finalizado','aprobada'].includes(t))return 'ok';
   if(['inactivo','inactiva','fuera de servicio','critica','critico','utilizacion critica','cancelado','rechazada'].includes(t))return 'danger';
   if(['alta utilizacion','degradado','degradada','pendiente por capacidad','pendiente de activacion','alta'].includes(t))return 'warning';
   return 'info';
 };
 document.querySelectorAll('.status').forEach(el=>{el.classList.remove('ok','info','warning','danger');el.classList.add(stateClass(el.textContent));});
 document.querySelectorAll('.crud-fields[disabled]').forEach(fields=>{
   const workflow=fields.closest('[data-workflow]');if(workflow){const state=fields.querySelector('select[name=estado],select[name=fase]');if(state){const clone=state.cloneNode(true);clone.id='workflow-state';clone.hidden=true;clone.dataset.readonly='true';workflow.append(clone);}}
   const name=fields.querySelector('#nombre')?.value;
   const role=fields.querySelector('#rolId')?.selectedOptions[0]?.textContent;
   if(name&&role){
     const hero=document.createElement('div');hero.className='user-profile-hero';
     const avatar=document.createElement('div');avatar.className='user-avatar';avatar.textContent=name.split(/\s+/).slice(0,2).map(s=>s[0]).join('');
     const heading=document.createElement('div');heading.className='user-profile-heading';
     const title=document.createElement('h3');title.textContent=name;heading.append(title);
     if(role){const p=document.createElement('p');p.textContent=role;heading.append(p);}
     hero.append(avatar,heading);fields.before(hero);
   }
   fields.querySelectorAll('.form-field').forEach(field=>{
     const control=field.querySelector('input,select,textarea'),label=field.querySelector('label');if(!control||!label)return;
     let value=control.tagName==='SELECT'?[...control.selectedOptions].map(o=>o.textContent).join(', '):control.value;
     if(value&&['date','datetime-local'].includes(control.type)){
       const date=new Date(control.type==='date'?value+'T00:00:00':value);
       if(!Number.isNaN(date.getTime()))value=new Intl.DateTimeFormat('es-PE',control.type==='date'||control.id==='fechaIngreso'?{day:'2-digit',month:'2-digit',year:'numeric'}:{dateStyle:'short',timeStyle:'short'}).format(date);
     }
     const text=document.createElement('span'),small=document.createElement('small'),strong=document.createElement('strong');
     small.textContent=label.textContent;strong.textContent=value||'Sin registro';
     if(/estado|condicion/i.test(normalize(label.textContent)))strong.className='status '+stateClass(value||'');
     text.append(small,strong);
     const icon=document.createElement('i');icon.className='fa-regular fa-file-lines';icon.setAttribute('aria-hidden','true');
     field.classList.add('profile-data');field.replaceChildren(icon,text);
   });
 });
 document.querySelectorAll('.table-container table').forEach(table=>{
   const alertHeader=[...table.querySelectorAll('thead th')].at(-1);
   if(alertHeader?.textContent.trim()==='Alerta'){
     [...table.tBodies[0].rows].forEach(row=>{row.dataset.utilizacion=row.cells[row.cells.length-1].textContent.trim();row.deleteCell(-1);});
     alertHeader.remove();
   }
   const headers=[...table.querySelectorAll('thead th')];
   const labels=headers.map(th=>th.textContent.trim());
   table.classList.add('responsive-table');table.closest('.table-container').classList.add('responsive-card-container');
   const labelRows=()=>[...table.tBodies].forEach(body=>[...body.rows].forEach(row=>[...row.cells].forEach((cell,i)=>cell.dataset.label=labels[i]||'')));
   labelRows();new MutationObserver(labelRows).observe(table.tBodies[0],{childList:true,subtree:true});
   [...table.tBodies[0].rows].forEach(row=>{
     [...row.cells].forEach((cell,i)=>{
       if(['Condición','Alerta','Estado','Estado actual','Severidad'].includes(labels[i])&&!cell.querySelector('.status')){
         const badge=document.createElement('span');badge.className='status '+stateClass(cell.textContent);badge.textContent=cell.textContent;cell.replaceChildren(badge);
       }
       if(labels[i]==='Utilización (%)'){
         const percent=Number(cell.textContent);if(!Number.isFinite(percent))return;
         const meter=document.createElement('div');meter.className='usage-meter';
         const badge=document.createElement('span');badge.className='status '+(percent<80?'ok':percent<=95?'warning':'danger');badge.textContent=percent.toLocaleString('es-PE',{maximumFractionDigits:1})+'% · '+(percent<80?'Normal':percent<=95?'Alta':'Crítica');badge.title=row.dataset.utilizacion||'';
         const progress=document.createElement('div');progress.className='progress';progress.setAttribute('role','meter');progress.setAttribute('aria-label','Utilización del segmento');progress.setAttribute('aria-valuemin','0');progress.setAttribute('aria-valuemax','100');progress.setAttribute('aria-valuenow',String(percent));
         const bar=document.createElement('span');bar.className=percent>95?'critical':percent>=80?'high':'';bar.style.width=Math.max(0,Math.min(100,percent))+'%';progress.append(bar);meter.append(badge,progress);cell.replaceChildren(meter);
       }
     });
   });
   const server=table.hasAttribute('data-server-sort');
   const allowed=location.pathname.includes('UsuarioServlet')?['nombre']:location.pathname.includes('EstacionServlet')?['nombre']:['codigo','nombre'];const sortable=labels.includes('Orden')?[]:server?headers.filter(h=>allowed.includes(h.dataset.sort)):headers.filter(h=>['Código','Segmento','Nombre','Utilización (%)'].includes(h.textContent.trim()));if(!sortable.length)return;
   let active=server?(params.get('orden')||(location.pathname.includes('UsuarioServlet')?'nombre':'codigo')):String(headers.indexOf(sortable[0])),direction=params.get('direccion')==='desc'?'desc':'asc';
   const choose=(key,dir)=>{
     if(server){const url=new URL(location.href);url.searchParams.set('orden',key);url.searchParams.set('direccion',dir);url.searchParams.delete('pagina');updateList(url);return;}
     active=key;direction=dir;const index=Number(key),body=table.tBodies[0];
     [...body.rows].sort((a,b)=>{
       const value=r=>r.cells[index]?.textContent.trim()||'';
       return value(a).localeCompare(value(b),'es',{numeric:true,sensitivity:'base'})*(dir==='desc'?-1:1);
     }).forEach(row=>body.append(row));table._refresh?.();update();
   };
   const mobile=document.createElement('label');mobile.className='sort-mobile';mobile.textContent='Ordenar por';
   const select=document.createElement('select');select.setAttribute('aria-label','Ordenar resultados');
   sortable.forEach(th=>{
     const key=server?th.dataset.sort:String(headers.indexOf(th));
     const name=labels[headers.indexOf(th)];
     const button=document.createElement('button');button.type='button';button.className='table-sort-button';button.textContent=name;
     const arrow=document.createElement('span');arrow.setAttribute('aria-hidden','true');button.append(arrow);th.replaceChildren(button);
     button.addEventListener('click',()=>choose(key,active===key&&direction==='asc'?'desc':'asc'));
     for(const dir of ['asc','desc'])select.add(new Option(name+' · '+(dir==='asc'?'ascendente':'descendente'),key+':'+dir));
   });
   document.addEventListener('oceanlink:list-updated',()=>{const current=new URLSearchParams(location.search);active=current.get('orden')||(location.pathname.includes('UsuarioServlet')?'nombre':'codigo');direction=current.get('direccion')==='desc'?'desc':'asc';update();});
   const update=()=>{sortable.forEach(th=>{const key=server?th.dataset.sort:String(headers.indexOf(th));th.setAttribute('aria-sort',active===key?(direction==='asc'?'ascending':'descending'):'none');th.querySelector('button span').textContent=active===key?(direction==='asc'?' ↑':' ↓'):' ↕';});select.value=active+':'+direction;};
   select.addEventListener('change',()=>{const [key,dir]=select.value.split(':');choose(key,dir);});mobile.append(select);table.closest('.table-container').before(mobile);update();
 });
});
