document.addEventListener('DOMContentLoaded',()=>{
 document.querySelector('[data-view-pdf]')?.addEventListener('click',async e=>{
  const button=e.currentTarget;button.disabled=true;
  try{
   const {PDFDocument,StandardFonts,rgb}=window.PDFLib,doc=await PDFDocument.create(),font=await doc.embedFont(StandardFonts.Helvetica);let page,y;
   const safe=s=>String(s||'').replace(/[^\x20-\x7e\xa0-\xff]/g,' - ');
   const next=()=>{page=doc.addPage([842,595]);y=552;page.drawText('OceanLink · Reporte operativo',{x:32,y,size:19,font,color:rgb(.03,.25,.35)});y-=28;};next();
   const subtitle=document.querySelector('.panel-heading p')?.textContent;page.drawText(safe(subtitle).slice(0,140),{x:32,y,size:10,font});y-=28;
   const table=document.querySelector('table'),headers=[...table.querySelectorAll('thead th')].map(h=>safe(h.textContent).replace(/[↑↕↓]/g,''));const rows=[headers,...[...table.tBodies[0].rows].filter(r=>r.cells.length===headers.length).map(r=>[...r.cells].map(c=>safe(c.textContent)))];const width=778/headers.length;
   for(const row of rows){const wrapped=row.map(text=>{const size=Math.max(10,Math.floor(width/5));return Array.from({length:Math.max(1,Math.ceil(text.length/size))},(_,i)=>text.slice(i*size,(i+1)*size));});const height=Math.max(...wrapped.map(x=>x.length))*13+12;if(y-height<32)next();wrapped.forEach((lines,i)=>lines.forEach((line,j)=>page.drawText(line,{x:32+i*width,y:y-j*13,size:9,font})));y-=height;page.drawLine({start:{x:32,y:y+4},end:{x:810,y:y+4},thickness:.4,color:rgb(.8,.86,.88)});}
   const bytes=await doc.save(),url=URL.createObjectURL(new Blob([bytes],{type:'application/pdf'}));const modal=document.createElement('dialog');modal.className='pdf-dialog';modal.innerHTML='<div class="modal-header"><h2>Vista PDF del reporte</h2><button class="secondary-button" type="button">Cerrar</button></div><iframe title="PDF del reporte"></iframe><a class="primary-button" download="OceanLink-reporte.pdf">Descargar PDF</a>';modal.querySelector('iframe').src=url;modal.querySelector('a').href=url;modal.querySelector('button').onclick=()=>modal.close();modal.addEventListener('close',()=>{URL.revokeObjectURL(url);modal.remove();});document.body.append(modal);modal.showModal();
  }catch(error){button.textContent='No se pudo generar el PDF. Reintentar';console.error(error);}finally{button.disabled=false;}
 });
if(new URLSearchParams(location.search).get('pdf')==='1')setTimeout(()=>document.querySelector('[data-view-pdf]')?.click(),0);});
