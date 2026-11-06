document.addEventListener('DOMContentLoaded',()=>{
 const all=(s,r=document)=>[...r.querySelectorAll(s)];
 all('.sidebar nav a').forEach(a=>{const target=new URL(a.href),current=new URL(location.href);a.classList.toggle('active',target.pathname===current.pathname&&['vista'].every(key=>(target.searchParams.get(key)||'')===(current.searchParams.get(key)||'')));});
 if(document.querySelector('.session-data .role')?.textContent.trim()==='Supervisor'){
  const back=document.querySelector('.back-row a');const route=location.pathname.split('/').pop();const views={SegmentoServlet:'capacidad',IncidenciaServlet:'incidencias',MantenimientoServlet:'mantenimientos'};
  if(back&&views[route]){back.href='ConsultaServlet?vista='+views[route]+'&menu=sup';back.textContent='← Volver a '+({capacidad:'red y capacidad',incidencias:'incidencias',mantenimientos:'mantenimientos'}[views[route]]);}
 }
 all('[data-initials]').forEach(e=>e.textContent=e.textContent.trim().split(/\s+/).slice(0,2).map(s=>s[0]).join(''));
 all('[data-profile-date]').forEach(el=>{const date=new Date(el.textContent.trim()+'T00:00:00');if(!Number.isNaN(date.getTime()))el.textContent=new Intl.DateTimeFormat('es-PE',{day:'2-digit',month:'2-digit',year:'numeric'}).format(date);});
 const modal=document.querySelector('#mi-perfil');let previous;
 const close=()=>{modal.hidden=true;modal.classList.remove('is-open');document.body.classList.remove('modal-open');previous?.focus();};
 all('[data-profile-open]').forEach(b=>b.addEventListener('click',()=>{previous=b;modal.hidden=false;modal.classList.add('is-open');document.body.classList.add('modal-open');modal.querySelector('.modal-close').focus();}));
 all('[data-profile-close]').forEach(b=>b.addEventListener('click',close));
 document.addEventListener('keydown',e=>{if(modal.hidden)return;if(e.key==='Escape')close();if(e.key==='Tab'){const items=all('button,input',modal).filter(x=>x.getClientRects().length&&!x.classList.contains('modal-background'));if(e.shiftKey&&document.activeElement===items[0]){e.preventDefault();items.at(-1).focus();}else if(!e.shiftKey&&document.activeElement===items.at(-1)){e.preventDefault();items[0].focus();}}});
 const showToast=(message,type='error')=>{let toast=document.querySelector('.app-toast');if(!toast){toast=document.createElement('div');toast.setAttribute('role','alert');document.body.append(toast);}toast.className='app-toast '+type;const icon=document.createElement('i');icon.className='fa-solid fa-circle-exclamation';const text=document.createElement('span');text.textContent=message;toast.replaceChildren(icon,text);requestAnimationFrame(()=>toast.classList.add('show'));clearTimeout(toast._timer);toast._timer=setTimeout(()=>toast.classList.remove('show'),4000);};
 window.OceanLinkToast=showToast;
 const confirmPhone=phone=>new Promise(resolve=>{
 const dialog=document.createElement('div');dialog.className='modal is-open profile-save-confirm';
 dialog.innerHTML='<button class="modal-background" type="button" aria-label="Volver"></button><div class="modal-box confirm-box" role="alertdialog" aria-modal="true" aria-labelledby="phone-confirm-title"><div class="confirm-icon"><i class="fa-solid fa-circle-question"></i></div><div class="confirm-copy"><h2 id="phone-confirm-title">Guardar celular</h2><p></p></div><div class="confirm-actions"><button type="button" class="danger-button" data-back>Cancelar</button><button type="button" class="primary-button" data-save>Confirmar</button></div></div>';
 dialog.querySelector('p').textContent='Tu número quedará como '+phone+'.';document.body.append(dialog);
 const previous=document.activeElement;const finish=result=>{document.removeEventListener('keydown',keys,true);dialog.remove();previous?.focus();resolve(result);};
 const keys=event=>{if(event.key==='Escape'){event.preventDefault();event.stopImmediatePropagation();finish(false);}if(event.key==='Tab'){event.preventDefault();event.stopImmediatePropagation();const buttons=[dialog.querySelector('[data-back]'),dialog.querySelector('[data-save]')];buttons[document.activeElement===buttons[0]?1:0].focus();}};
 document.addEventListener('keydown',keys,true);dialog.querySelector('[data-back]').onclick=()=>finish(false);dialog.querySelector('.modal-background').onclick=()=>finish(false);dialog.querySelector('[data-save]').onclick=()=>finish(true);dialog.querySelector('[data-back]').focus();
 });
 document.querySelector('[data-profile-form]')?.addEventListener('submit',async e=>{e.preventDefault();const form=e.currentTarget,status=document.querySelector('[data-profile-status]');const prefix=form.telefonoRegion.selectedOptions[0]?.dataset.prefix||'';if(form.dataset.saving)return;form.dataset.saving='true';try{if(!await confirmPhone(prefix+' '+form.telefono.value))return;const response=await fetch(form.action,{method:'POST',body:new URLSearchParams(new FormData(form))});if(response.redirected&&new URL(response.url).pathname.endsWith('/LoginServlet')){location.assign(response.url);return;}const message=await response.text();showToast(message,response.ok?'success':'error');if(response.ok){document.querySelector('[data-profile-phone]').textContent=form.telefono.value ? prefix+' '+form.telefono.value : 'Sin registrar';form.dispatchEvent(new Event('profile-saved'));}}catch{showToast('No se pudo guardar. Inténtalo nuevamente.','error');}finally{delete form.dataset.saving;}});
 all('.history-tab').forEach(b=>b.addEventListener('click',()=>{all('.history-tab').forEach(t=>{t.classList.toggle('active',t===b);t.setAttribute('aria-selected',String(t===b));});all('.history-tab-panel').forEach(p=>p.classList.toggle('active',p.dataset.historyPanel===b.dataset.historyTab));}));
 const initialTab=all('.history-tab').find(t=>t.dataset.historyTab===location.hash.slice(1));initialTab?.click();
 all('.filter-panel').filter(p=>!p.closest('form')&&!p.classList.contains('history-global-filters')).forEach(panel=>{
  const article=panel.closest('article'),table=article?.querySelector('table')||panel.parentElement.querySelector('table');if(!table)return;
  const controls=all('input,select',panel);const filter=()=>{
   all('tbody tr',table).forEach(row=>{
    const text=row.textContent.toLocaleLowerCase('es');row.dataset.portalMatch=String(controls.every(c=>{
     const value=c.value.trim().toLocaleLowerCase('es');if(!value||/^(todos|todas|toda la red)$/.test(value))return true;
     if(c.type==='date'){const dates=text.match(/\d{2}\/\d{2}\/\d{4}/g)||[];return dates.some(d=>{const [day,month,year]=d.split('/');const iso=`${year}-${month}-${day}`;return /desde|inicial/i.test(c.previousElementSibling?.textContent||'')?iso>=value:iso<=value;});}
     if(/^\d+$/.test(value)){const code=row.cells[0]?.textContent.trim()||'';const suffix=code.match(/-(\d+)$/);return Boolean(suffix)&&Number(suffix[1])===Number(value);}return text.includes(value.replace(/\s*\(.*$/,''));
    }));
   });table._refresh?.();
  };
  panel.querySelector('.filter-submit,.primary-button')?.addEventListener('click',filter);
 });
 const chart=document.querySelector('.history-line-chart');
 if(chart){
  const points=all('[data-chart-date]',chart).map(el=>({date:el.dataset.chartDate,value:Number(el.dataset.chartValue),segment:el.dataset.chartSegment}));
  const render=()=>{
   const from=document.querySelector('.history-date-start')?.value||'',to=document.querySelector('.history-date-end')?.value||'';
   const selected=document.querySelector('.history-segment-select')?.value||'';
   const filtered=points.filter(p=>(!from||p.date>=from)&&(!to||p.date<=to)&&(!/^\d+$/.test(selected)||p.segment===selected));
   const grouped=new Map();filtered.forEach(p=>{const a=grouped.get(p.date)||[];a.push(p.value);grouped.set(p.date,a);});const rows=[...grouped].map(([date,values])=>({date,value:values.reduce((a,b)=>a+b,0)/values.length}));chart.replaceChildren();
   const metrics=all('[data-history-metric]');const avg=rows.reduce((a,b)=>a+b.value,0)/(rows.length||1),max=Math.max(0,...rows.map(r=>r.value));
   [avg.toFixed(1)+'%',max.toFixed(1)+'%',rows.length?((rows.at(-1).value-rows[0].value).toFixed(1)+' pp'):'—'].forEach((text,i)=>{if(metrics[i])metrics[i].textContent=text;});
   if(!rows.length){chart.textContent='No hay registros para el periodo seleccionado.';return;}
   const svg=document.createElementNS('http://www.w3.org/2000/svg','svg');svg.setAttribute('viewBox','0 0 800 230');svg.setAttribute('role','img');svg.setAttribute('aria-label','Utilización histórica por fecha');
   const line=document.createElementNS(svg.namespaceURI,'polyline');line.setAttribute('points',rows.map((p,i)=>`${40+i*720/Math.max(1,rows.length-1)},${190-p.value*1.6}`).join(' '));line.setAttribute('fill','none');line.setAttribute('stroke','#0e9aa4');line.setAttribute('stroke-width','3');svg.append(line);
   rows.forEach((p,i)=>{const dot=document.createElementNS(svg.namespaceURI,'circle');dot.setAttribute('cx',40+i*720/Math.max(1,rows.length-1));dot.setAttribute('cy',190-p.value*1.6);dot.setAttribute('r','4');dot.setAttribute('fill','#0e9aa4');const title=document.createElementNS(svg.namespaceURI,'title');title.textContent=p.date+' · '+p.value+'%';dot.append(title);svg.append(dot);});chart.append(svg);
  };
  const start=document.querySelector('.history-date-start'),end=document.querySelector('.history-date-end');if(points.length){start.value=points[0].date;end.value=points.at(-1).date;}
  document.querySelector('.history-filter-submit')?.addEventListener('click',render);render();
 }
 const histories=[['incidents','incidencias'],['maintenance','mantenimientos']];
 histories.forEach(([key,panelName])=>{
  const panel=document.querySelector(`[data-history-panel="${panelName}"]`);if(!panel)return;
  const events=all('[data-event-date]',panel).map(e=>({...e.dataset}));
  const render=()=>{
   const from=document.querySelector('.history-date-start').value,to=document.querySelector('.history-date-end').value;
   const select=document.querySelector('.history-segment-select');const selected=select?.value||'';
   const rows=events.filter(e=>(!from||e.eventDate>=from)&&(!to||e.eventDate<=to)&&(!/^\d+$/.test(selected)||(e.eventSegment||'').split(',').includes(selected)));
   const code=select?.selectedOptions[0]?.textContent||'';
   all('table tbody tr',panel).forEach(row=>{const text=row.textContent;const dates=(text.match(/\d{2}\/\d{2}\/\d{4}/g)||[]).map(d=>d.split('/').reverse().join('-'));row.dataset.portalMatch=String((!/^\d+$/.test(selected)||text.includes(code))&&dates.some(d=>(!from||d>=from)&&(!to||d<=to)));});panel.querySelector('table')?._refresh?.();
   const metrics=all('.history-kpi strong',panel);if(metrics[0])metrics[0].textContent=rows.length;if(metrics[1])metrics[1].textContent=rows.filter(e=>e.eventStatus===(key==='incidents'?'CERRADA':'FINALIZADO')).length;if(metrics[2])metrics[2].textContent=rows.filter(e=>key==='incidents'?e.eventType==='CRITICA':e.eventStatus==='PROGRAMADO').length;
   if(metrics[3])metrics[3].textContent=new Set(rows.flatMap(e=>(e.eventServices||'').split(',')).filter(Boolean)).size;const chart=panel.querySelector('.history-stat-chart'),distribution=panel.querySelector('.history-distribution');chart.replaceChildren();distribution.replaceChildren();
   for(const [field,target] of [['eventType',chart],['eventStatus',distribution]]){
    const counts=new Map();rows.forEach(e=>counts.set(field==='eventType'?(()=>{const d=new Date(e.eventDate+'T12:00:00Z');d.setUTCDate(d.getUTCDate()-((d.getUTCDay()+6)%7));return d.toISOString().slice(0,10)+' · '+e[field];})():e[field],(counts.get(field==='eventType'?(()=>{const d=new Date(e.eventDate+'T12:00:00Z');d.setUTCDate(d.getUTCDate()-((d.getUTCDay()+6)%7));return d.toISOString().slice(0,10)+' · '+e[field];})():e[field])||0)+1));
    if(!rows.length){target.textContent='No hay registros para el periodo seleccionado.';continue;}
    for(const [label,count] of counts){const line=document.createElement('div');line.className='history-real-bar';const text=document.createElement('span');text.textContent=label.replaceAll('_',' ')+' · '+count;const bar=document.createElement('span');bar.style.width=(count/rows.length*100)+'%';line.append(text,bar);target.append(line);}
   }
  };
  document.querySelector('.history-filter-submit')?.addEventListener('click',render);render();
 });
});
