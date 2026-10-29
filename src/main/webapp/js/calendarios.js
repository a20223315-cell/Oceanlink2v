document.addEventListener('DOMContentLoaded',()=>{const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];const closeCustomSelects=()=>document.querySelectorAll('.ol-select-menu.open,.ol-select.open').forEach(e=>e.classList.remove('open'));
  const datePickers=[];
  const parseIsoDateOnly=value=>{const m=String(value||'').slice(0,10).match(/^(\d{4})-(\d{2})-(\d{2})$/);return m?new Date(Number(m[1]),Number(m[2])-1,Number(m[3])):null;};
  const isoDateOnly=d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  const displayDateOnly=value=>{const d=parseIsoDateOnly(value);return d?`${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}/${d.getFullYear()}`:'Seleccionar fecha';};
  const dateAtMidnight=d=>new Date(d.getFullYear(),d.getMonth(),d.getDate());
  const closeDatePickers=except=>datePickers.forEach(item=>{if(item.picker!==except)item.picker.classList.remove('open');});
  const enhanceDateInput=input=>{
    if(!input||input.dataset.dateEnhanced==='1'||input.disabled||input.readOnly||input.closest('fieldset')?.disabled)return;
    const withTime=input.type==='datetime-local';input.dataset.dateEnhanced='1'; input.classList.add('native-date-hidden');
    const button=document.createElement('button');button.type='button';button.className='date-display';button.innerHTML='<span class="date-display-value"></span><i class="fa-regular fa-calendar"></i>';
    input.insertAdjacentElement('afterend',button);
    const picker=document.createElement('div');picker.className='ol-datetime ol-date-only';picker.setAttribute('role','dialog');picker.setAttribute('aria-label','Seleccionar fecha');
    picker.innerHTML=`<div class="datetime-header"><button class="datetime-nav date-prev" type="button" aria-label="Mes anterior"><i class="fa-solid fa-chevron-left"></i></button><strong class="date-month-title"></strong><button class="datetime-nav date-next" type="button" aria-label="Mes siguiente"><i class="fa-solid fa-chevron-right"></i></button></div><div class="datetime-weekdays"><span>LU</span><span>MA</span><span>MI</span><span>JU</span><span>VI</span><span>SÁ</span><span>DO</span></div><div class="datetime-days date-days"></div><div class="datetime-footer"><button class="datetime-today date-today" type="button">Hoy</button><span class="datetime-footer-spacer"></span><button class="datetime-cancel date-cancel" type="button">Cancelar</button><button class="datetime-apply date-apply" type="button">Aplicar</button></div>`;
    if(withTime){const time=document.createElement('input');time.type='time';time.className='calendar-time';time.setAttribute('aria-label','Hora');picker.querySelector('.datetime-footer').before(time);}document.body.appendChild(picker);
    const bounds=()=>({min:parseIsoDateOnly(input.min),max:parseIsoDateOnly(input.max)});
    const isBlocked=date=>{const d=dateAtMidnight(date);const {min,max}=bounds();return Boolean((min&&d<dateAtMidnight(min))||(max&&d>dateAtMidnight(max)));};
    const clamp=date=>{let d=dateAtMidnight(date);const {min,max}=bounds();if(min&&d<dateAtMidnight(min))d=dateAtMidnight(min);if(max&&d>dateAtMidnight(max))d=dateAtMidnight(max);return d;};
    let selected=clamp(parseIsoDateOnly(input.value)||new Date());let view=new Date(selected.getFullYear(),selected.getMonth(),1);
    const sync=()=>{const value=$('.date-display-value',button);if(value)value.textContent=displayDateOnly(input.value)+(withTime&&input.value?' · '+input.value.slice(11,16):'');button.classList.toggle('placeholder',!input.value);};
    const monthAllowed=(year,month)=>{const first=new Date(year,month,1);const last=new Date(year,month+1,0);const {min,max}=bounds();if(min&&last<dateAtMidnight(min))return false;if(max&&first>dateAtMidnight(max))return false;return true;};
    const render=()=>{
      const months=['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];$('.date-month-title',picker).textContent=`${months[view.getMonth()]} ${view.getFullYear()}`;
      const prevBtn=$('.date-prev',picker),nextBtn=$('.date-next',picker);if(prevBtn)prevBtn.disabled=!monthAllowed(view.getFullYear(),view.getMonth()-1);if(nextBtn)nextBtn.disabled=!monthAllowed(view.getFullYear(),view.getMonth()+1);
      const grid=$('.date-days',picker);grid.innerHTML='';const first=(new Date(view.getFullYear(),view.getMonth(),1).getDay()+6)%7;const count=new Date(view.getFullYear(),view.getMonth()+1,0).getDate();const prev=new Date(view.getFullYear(),view.getMonth(),0).getDate();
      for(let i=0;i<42;i++){const offset=i-first+1;let day,month=view.getMonth(),year=view.getFullYear(),muted=false;if(offset<1){day=prev+offset;month--;muted=true}else if(offset>count){day=offset-count;month++;muted=true}else day=offset;const candidate=new Date(year,month,day);const blocked=isBlocked(candidate);const b=document.createElement('button');b.type='button';b.className='datetime-day';if(muted)b.classList.add('muted');if(blocked){b.disabled=true;b.classList.add('date-disabled');b.setAttribute('aria-disabled','true');}if(candidate.toDateString()===selected.toDateString())b.classList.add('selected');const today=dateAtMidnight(new Date());if(candidate.toDateString()===today.toDateString())b.classList.add('today');b.textContent=String(day);if(!blocked)b.addEventListener('click',event=>{event.stopPropagation();selected=candidate;view=new Date(selected.getFullYear(),selected.getMonth(),1);render();});grid.appendChild(b);}
      const todayBtn=$('.date-today',picker);if(todayBtn)todayBtn.disabled=isBlocked(new Date());
      const applyBtn=$('.date-apply',picker);if(applyBtn)applyBtn.disabled=isBlocked(selected);
    };
    button.addEventListener('click',event=>{
      event.stopPropagation();closeCustomSelects();const opening=!picker.classList.contains('open');closeDatePickers(picker);
      if(opening){
        if(input.dataset.defaultToday==='1'){const today=isoDateOnly(new Date());input.max=today;if(!input.value){input.value=today;sync();}}
        selected=clamp(parseIsoDateOnly(input.value)||new Date());view=new Date(selected.getFullYear(),selected.getMonth(),1);render();if(withTime)picker.querySelector('.calendar-time').value=input.value.slice(11,16)||'00:00';picker.classList.add('open');
      }
    });
    $('.date-prev',picker).addEventListener('click',()=>{if($('.date-prev',picker).disabled)return;view=new Date(view.getFullYear(),view.getMonth()-1,1);render();});
    $('.date-next',picker).addEventListener('click',()=>{if($('.date-next',picker).disabled)return;view=new Date(view.getFullYear(),view.getMonth()+1,1);render();});
    $('.date-today',picker).addEventListener('click',()=>{const now=dateAtMidnight(new Date());if(isBlocked(now))return;selected=now;view=new Date(selected.getFullYear(),selected.getMonth(),1);render();});
    $('.date-cancel',picker).addEventListener('click',()=>picker.classList.remove('open'));
    $('.date-apply',picker).addEventListener('click',()=>{if(isBlocked(selected))return;input.value=isoDateOnly(selected)+(withTime?'T'+(picker.querySelector('.calendar-time').value||'00:00'):'');input.dispatchEvent(new Event('input',{bubbles:true}));input.dispatchEvent(new Event('change',{bubbles:true}));sync();picker.classList.remove('open');});
    input._syncDateDisplay=sync;input._refreshDatePicker=()=>{selected=clamp(parseIsoDateOnly(input.value)||new Date());view=new Date(selected.getFullYear(),selected.getMonth(),1);sync();if(picker.classList.contains('open'))render();};datePickers.push({input,button,picker});sync();
    input.closest('form')?.addEventListener('reset',()=>setTimeout(sync,0));
  };
  $$('input[type="date"],input[type="datetime-local"]').forEach(enhanceDateInput);
document.addEventListener('click',event=>{if(!event.target.closest('.ol-date-only,.date-display'))closeDatePickers();});document.addEventListener('keydown',event=>{if(event.key==='Escape'&&datePickers.some(item=>item.picker.classList.contains('open'))){event.preventDefault();event.stopImmediatePropagation();closeDatePickers();}},true);document.addEventListener('invalid',event=>{const item=datePickers.find(item=>item.input===event.target);if(item){event.preventDefault();item.button.focus();item.button.title=item.input.validationMessage;}},true);
});
