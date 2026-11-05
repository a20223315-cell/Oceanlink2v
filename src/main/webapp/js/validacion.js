document.addEventListener('DOMContentLoaded',()=>{
 const fields=form=>[...form.querySelectorAll('input,select,textarea')].filter(f=>f.willValidate);
 const label=field=>field.labels?.[0]?.textContent.trim()||field.getAttribute('aria-label')||'Este campo';
 const message=field=>{
  const v=field.validity,name=label(field),value=field.value.trim();
  if(field.type==='tel'&&field.form?.querySelector('[name=telefonoRegion]')){
   const region=field.form.querySelector('[name=telefonoRegion]').value,length={PE:9,CL:9,CO:10,PA:8}[region];
   if(!length)return 'Seleccione un prefijo.';
   if(!value)return 'Ingrese su número de celular.';
   return /^[0-9 ]+$/.test(value)&&value.replace(/ /g,'').length===length?'':'Ingrese '+length+' dígitos.';
  }
  if(field.name==='telefonoRegion'&&!value)return 'Seleccione un prefijo.';
  if(field.required&&!value)return (field.matches('select')?'Seleccione ':'Ingrese ')+({Apellidos:'sus apellidos',Nombres:'sus nombres',Teléfono:'su teléfono',Celular:'su celular',País:'un país',Rol:'un rol',Correo:'un correo',Nombre:'un nombre',Observación:'una observación',Descripción:'una descripción'}[name]||name.toLocaleLowerCase('es'))+'.';
  if(v.valueMissing)return 'Seleccione '+name.toLocaleLowerCase('es')+'.';
  if(v.badInput)return name+': ingresa un número válido.';
  if(v.rangeUnderflow)return Number(field.min)>0&&Number(field.value)<=0?name+': debe ser mayor que 0.':name+': el valor mínimo es '+field.min+'.';
  if(v.rangeOverflow)return name+': el valor máximo permitido es '+field.max+'.';
  if(v.stepMismatch)return name+': respeta los decimales permitidos (incremento '+field.step+').';
  if(v.typeMismatch)return name+': ingresa un formato válido'+(field.type==='email'?', por ejemplo nombre@empresa.com.':'.');
  if(v.patternMismatch)return name+': revisa el formato ingresado.';
  if(v.tooLong)return name+': máximo '+field.maxLength+' caracteres.';
  if(v.tooShort)return name+': mínimo '+field.minLength+' caracteres.';
  if(['nombres','apellidos'].includes(field.name)&&value&&!/^[\p{L}\p{M}]+(?:[ '’-][\p{L}\p{M}]+)*$/u.test(value))return 'Ingrese '+(field.name==='nombres'?'nombres':'apellidos')+' sin números ni símbolos inválidos.';
  if(field.type==='tel'&&value){const prefix=field.form?.querySelector('[data-phone-prefix],.field-prefix')?.textContent.trim(),length={'+51':9,'+56':9,'+57':10,'+507':8}[prefix];if(!/^[0-9 ()+\-]+$/.test(value))return 'Ingrese un teléfono válido.';if(length&&value.replace(/\D/g,'').length!==length)return 'Ingrese '+length+' dígitos.';}
  return '';
 };
 let counter=0, focusingError=false;const errors=new Map();
 const visualControl=field=>field.matches('select')?field.nextElementSibling?.querySelector('.ol-select-trigger')||field:field.nextElementSibling?.matches('.date-display')?field.nextElementSibling:field;
 const clear=field=>{field.setCustomValidity?.('');field.removeAttribute('aria-invalid');const control=visualControl(field);control.removeAttribute('aria-invalid');control.removeAttribute('title');const item=errors.get(field);if(!item)return;item.node.remove();item.anchor.classList.remove('validation-active');errors.delete(field);if(![...errors.values()].some(error=>error.host===item.host))item.host.classList.remove('field-invalid');for(const target of new Set([field,control])){const ids=(target.getAttribute('aria-describedby')||'').split(' ').filter(id=>id&&id!==item.node.id);if(ids.length)target.setAttribute('aria-describedby',ids.join(' '));else target.removeAttribute('aria-describedby');}};
 const show=(field,text)=>{
  clear(field);
  const host=field.closest('.form-field')||field.closest('.phone-field')?.parentElement||field.parentElement,control=visualControl(field),height=control.getBoundingClientRect().height;
  const group=field.matches('[type=checkbox],[type=radio]');
  let anchor=group?field.closest('.segment-choice-grid,.checkbox-group')||host:control.closest('.validation-control,.phone-field,.user-email-field,.ol-select');
  if(!anchor){anchor=document.createElement('div');anchor.className='validation-control';field.before(anchor);anchor.append(field);if(control!==field)anchor.append(control);}
  anchor.classList.add('validation-anchor','validation-active');
  anchor.style.setProperty('--validation-height',height+'px');
  if(group)anchor.classList.add('validation-group');
  const node=document.createElement('span');node.className=field.name==='telefonoRegion'?'field-feedback prefix-feedback':'field-feedback';node.id='field-feedback-'+(++counter);node.setAttribute('role','alert');node.textContent=text;anchor.append(node);host.classList.add('field-invalid');
  if(!group){const style=getComputedStyle(control),box=control.getBoundingClientRect(),frame=anchor.getBoundingClientRect();node.style.font=style.font;node.style.letterSpacing=style.letterSpacing;node.style.left=(box.left-frame.left+parseFloat(style.paddingLeft||0))+'px';node.style.right=(frame.right-box.right+parseFloat(style.paddingRight||0))+'px';const prefix=anchor.matches('.phone-field')?anchor.querySelector('.field-prefix,[data-phone-prefix]'):null;if(prefix)node.style.left=Math.ceil(Math.max(box.left-frame.left+parseFloat(style.paddingLeft||0),prefix.getBoundingClientRect().right-frame.left+14))+'px';if(field.matches('textarea')){node.style.top=style.paddingTop;node.style.transform='none';}}
  for(const target of new Set([field,control])){target.setAttribute('aria-invalid','true');target.setAttribute('aria-describedby',((target.getAttribute('aria-describedby')||'')+' '+node.id).trim());}
  errors.set(field,{node,host,anchor});
 };
 const focus=field=>{const host=field.closest('.form-field')||field.parentElement;const proxy=field.matches('select')?host.querySelector('.ol-select-trigger'):field.nextElementSibling?.matches('.date-display')?field.nextElementSibling:null;focusingError=true;try{(proxy||field).focus({preventScroll:true});}finally{focusingError=false;}host.scrollIntoView({block:'center',behavior:'smooth'});};
 const validate=form=>{let first=null;for(const field of fields(form)){clear(field);const text=message(field);if(text){show(field,text);first??=field;}}
  const segments=[...form.querySelectorAll('input[name=segmentos]')].filter(f=>!f.disabled);if(segments.length&&!segments.some(f=>f.checked)){show(segments[0],'Selecciona al menos un segmento.');first??=segments[0];}
  const station=form.querySelector('select[name=estacionId]'),segment=form.querySelector('select[name=segmentoId]');if(station&&segment&&!station.disabled&&!segment.disabled&&Boolean(station.value)===Boolean(segment.value)){show(station,'Selecciona una estación o un segmento, solo uno.');first??=station;}
  const start=form.querySelector('[name=inicio]'),end=form.querySelector('[name=fin]');if(start?.value&&end?.value&&end.value<=start.value){show(end,'La fecha y hora de fin debe ser posterior al inicio.');first??=end;}
  if(first){focus(first);return false;}return true;
 };
 document.querySelectorAll('form:not(.login-form)').forEach(form=>form.noValidate=true);
 document.addEventListener('submit',event=>{const form=event.target;if(!form.matches('form')||form.matches('.login-form'))return;if(!validate(form)){event.preventDefault();event.stopImmediatePropagation();}},true);
 const clearRelated=field=>{clear(field);const form=field.form;if(!form)return;if(field.name==='telefonoRegion'){const phone=form.querySelector('input[name=telefono]');if(phone&&errors.has(phone)){const text=message(phone);if(text)show(phone,text);else clear(phone);}}if(field.name==='segmentos'&&form.querySelector('input[name=segmentos]:checked'))form.querySelectorAll('input[name=segmentos]').forEach(clear);if(['estacionId','segmentoId'].includes(field.name)){const a=form.querySelector('select[name=estacionId]'),b=form.querySelector('select[name=segmentoId]');if(a&&b&&Boolean(a.value)!==Boolean(b.value)){clear(a);clear(b);}}if(['inicio','fin'].includes(field.name)){const a=form.querySelector('[name=inicio]'),b=form.querySelector('[name=fin]');if(a?.value&&b?.value&&b.value>a.value)clear(b);}};
 document.addEventListener('input',event=>{if(!focusingError&&event.target.matches('input,select,textarea'))clearRelated(event.target);});
 document.addEventListener('change',event=>{if(!focusingError&&event.target.matches('input,select,textarea'))clearRelated(event.target);});
 document.addEventListener('invalid',event=>{if(event.target.closest('.login-form'))return;event.preventDefault();const text=message(event.target);if(text)show(event.target,text);},true);
 document.addEventListener('reset',event=>{for(const field of [...errors.keys()])if(field.form===event.target)clear(field);});
 
 const resumeEditing=event=>{
  if(focusingError||!(event.target instanceof Element))return;
  if(event.type==='keydown'&&(event.key==='Tab'||event.key==='Escape'||['Shift','Control','Alt','Meta'].includes(event.key)))return;
  for(const [field,item] of [...errors])if(field===event.target||item.anchor.contains(event.target)){if(field===event.target&&field.type==='tel')field.value='';clear(field);}
 };
 for(const type of ['pointerdown','mousedown','focusin','keydown','beforeinput','paste','cut','drop','compositionstart'])document.addEventListener(type,resumeEditing,true);
 window.OceanLinkValidation={show,clear,focus};
});
