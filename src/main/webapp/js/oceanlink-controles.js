document.addEventListener('DOMContentLoaded',()=>{
const $=(s,r=document)=>r.querySelector(s), $$=(s,r=document)=>Array.from(r.querySelectorAll(s));
  const customSelects = new Map();
  const flagsBase=new URL('../assets/flags/',document.querySelector('script[src*="oceanlink-controles.js"]').src);
  const norm=s=>s.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase();
  const codeFor=option=>option?.dataset.country||option?.textContent.match(/^([A-Z]{2}) · /)?.[1];
  const optionContent=(target,option,compact=false)=>{target.replaceChildren();const code=codeFor(option);if(code){const img=document.createElement('img');img.src=new URL(code.toLowerCase()+'.svg',flagsBase);img.alt='';img.className='country-flag';target.append(img);}target.append(document.createTextNode(compact&&option?.dataset.prefix?option.dataset.prefix:(option?.textContent.trim().replace(/^[A-Z]{2} · /,'')||'Seleccione')));};
  const closeCustomSelect = widget => {
    if (!widget) return;
    widget.classList.remove('open');
    widget._menu?.classList.remove('open');
    widget._trigger?.setAttribute('aria-expanded','false');
  };
  const syncCustomSelect = select => {
    const widget = customSelects.get(select); if (!widget) return;
    const option = select.options[select.selectedIndex];
    optionContent($('.ol-select-value',widget),option,select.name==='telefonoRegion');
    $$('.ol-select-option', widget._menu || document).forEach((item,index)=>(item.classList.toggle('selected',index===select.selectedIndex),item.setAttribute('aria-selected',String(index===select.selectedIndex))));
  };
  const closeCustomSelects = except => customSelects.forEach(widget => { if (widget !== except) closeCustomSelect(widget); });
  const positionCustomSelect = widget => {
    const trigger=widget?._trigger, menu=widget?._menu;
    if(!trigger||!menu||!widget.classList.contains('open')) return;
    const rect=trigger.getBoundingClientRect();
    if(rect.width<4 || rect.bottom<0 || rect.top>window.innerHeight){ closeCustomSelect(widget); return; }
    const margin=12, gap=8;
    const width=Math.min(Math.max(rect.width, 0, menu.classList.contains('ol-select-menu--compact') ? 112 : 0), window.innerWidth-margin*2);
    let left=Math.max(margin, Math.min(rect.left, window.innerWidth-width-margin));
    menu.style.width=`${width}px`;
    menu.style.left=`${left}px`;
    menu.style.maxHeight=`${Math.max(120,Math.min(320,window.innerHeight-margin*2))}px`;
    const desired=Math.min(menu.scrollHeight||260,320);
    const below=Math.max(0,window.innerHeight-rect.bottom-gap-margin);
    const above=Math.max(0,rect.top-gap-margin);
    const openAbove=below<Math.min(desired,190) && above>below;
    const available=Math.max(120,Math.min(320,openAbove?above:below));
    menu.style.maxHeight=`${available}px`;
    const top=openAbove ? Math.max(margin,rect.top-gap-Math.min(desired,available)) : Math.min(window.innerHeight-margin-available,rect.bottom+gap);
    menu.style.top=`${Math.max(margin,top)}px`;
    menu.classList.toggle('opens-upward',openAbove);
  };
  const enhanceSelect = select => {
    if (!select || select.multiple || select.dataset.enhanced === '1') return;
    select.dataset.enhanced='1';
    const widget=document.createElement('div'); widget.className='ol-select';
    const trigger=document.createElement('button'); trigger.disabled=select.disabled; trigger.type='button'; trigger.className='ol-select-trigger'; trigger.setAttribute('aria-haspopup','listbox'); trigger.setAttribute('aria-expanded','false'); trigger.innerHTML='<span class="ol-select-value"></span><i class="fa-solid fa-chevron-down"></i>';trigger.setAttribute('aria-label',select.getAttribute('aria-label')||select.labels?.[0]?.textContent.trim()||'Seleccionar opción');select.tabIndex=-1;select.setAttribute('aria-hidden','true');
    const menu=document.createElement('div'); menu.className='ol-select-menu ol-select-menu--portal';if(Array.from(select.options).some(o=>codeFor(o)))menu.classList.add('flag-select-menu');if(select.name==='telefonoRegion')menu.classList.add('prefix-select-menu'); menu.setAttribute('role','listbox'); if(select.closest('.pagination-size,.datetime-time')) menu.classList.add('ol-select-menu--compact');
    Array.from(select.options).forEach((option,index)=>{
      const item=document.createElement('button'); item.type='button'; item.className='ol-select-option'; item.setAttribute('role','option'); item.disabled=option.disabled;if(!option.value&&(select.name==='telefonoRegion'||Array.from(select.options).some(o=>codeFor(o))))item.hidden=true; optionContent(item,option,select.name==='telefonoRegion');item.title=option.textContent.trim();item.setAttribute('aria-label',option.textContent.trim());item.dataset.search=norm(option.textContent);
      item.addEventListener('click',event=>{event.stopPropagation();select.selectedIndex=index;select.dispatchEvent(new Event('change',{bubbles:true}));closeCustomSelect(widget);trigger.focus();});
      menu.appendChild(item);
    });
    select.classList.add('native-select-hidden');
    select.insertAdjacentElement('afterend',widget); widget.append(trigger); document.body.appendChild(menu);
    widget._menu=menu; widget._trigger=trigger; widget._select=select; customSelects.set(select,widget); syncCustomSelect(select);
    trigger.addEventListener('click',event=>{
      event.stopPropagation(); const opening=!widget.classList.contains('open'); closeCustomSelects(widget);
      if(opening){widget.classList.add('open');menu.classList.add('open');trigger.setAttribute('aria-expanded','true');positionCustomSelect(widget);requestAnimationFrame(()=>{positionCustomSelect(widget);});}
      else closeCustomSelect(widget);
    });
    trigger.addEventListener('keydown',event=>{
      if(!['ArrowDown','ArrowUp','Enter',' '].includes(event.key))return;
      if(!widget.classList.contains('open')){event.preventDefault();trigger.click();return;}
      if(event.key==='ArrowDown'||event.key==='ArrowUp'){
        event.preventDefault();const items=$$('.ol-select-option:not([hidden]):not([disabled])',menu);let idx=items.indexOf(document.activeElement);if(idx<0)idx=select.selectedIndex;idx=(idx+(event.key==='ArrowDown'?1:-1)+items.length)%items.length;items[idx]?.focus();
      }
    });
    let typed='',typedTimer;
    trigger.addEventListener('keydown',event=>{if(event.key.length===1&&event.key!==' '&&!event.ctrlKey&&!event.metaKey){typed+=norm(event.key);clearTimeout(typedTimer);typedTimer=setTimeout(()=>typed='',700);const found=Array.from(select.options).findIndex(o=>o.value&&!o.disabled&&(norm(o.textContent).startsWith(typed)||(o.dataset.prefix||'').startsWith(typed)));if(found>=0){event.preventDefault();select.selectedIndex=found;select.dispatchEvent(new Event('change',{bubbles:true}));}}});
    menu.addEventListener('keydown',event=>{
      const items=$$('.ol-select-option:not([hidden]):not([disabled])',menu);const idx=items.indexOf(document.activeElement);
      if(event.key==='Escape'){event.preventDefault();closeCustomSelect(widget);trigger.focus();}
      if(event.key==='ArrowDown'||event.key==='ArrowUp'){event.preventDefault();const next=(Math.max(idx,0)+(event.key==='ArrowDown'?1:-1)+items.length)%items.length;items[next]?.focus();}
    });
    select.addEventListener('change',()=>syncCustomSelect(select));
  };
  $$('select').forEach(enhanceSelect);
  document.addEventListener('click',event=>{if(!event.target.closest('.ol-select')&&!event.target.closest('.ol-select-menu--portal'))closeCustomSelects();});
  document.addEventListener('keydown',event=>{if(event.key==='Escape')closeCustomSelects();});
  window.addEventListener('resize',()=>customSelects.forEach(positionCustomSelect));
  document.addEventListener('scroll',()=>customSelects.forEach(positionCustomSelect),true);
  $$('form').forEach(form=>form.addEventListener('reset',()=>setTimeout(()=>$$('select',form).forEach(syncCustomSelect),0)));

const refreshSelect=select=>{const widget=customSelects.get(select);if(widget){closeCustomSelect(widget);widget._menu.remove();widget.remove();customSelects.delete(select);}delete select.dataset.enhanced;enhanceSelect(select);};
document.addEventListener('invalid',event=>{const widget=customSelects.get(event.target);if(widget){event.preventDefault();widget._trigger.focus();widget._trigger.setAttribute('aria-invalid','true');widget._trigger.title=event.target.validationMessage;}},true);
window.OceanLinkControls={enhanceSelect,refreshSelect};
});
