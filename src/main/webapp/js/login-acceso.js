document.addEventListener('DOMContentLoaded',()=>{
  const destinations={
    'dashboard.html':'ConsultaServlet?vista=general&area=admin',
    'cp_capacity.html':'ConsultaServlet?vista=capacidad&area=cp',
    'no_network.html':'ConsultaServlet?vista=red&area=no',
    'no_incidents.html':'IncidenciaServlet',
    'mc_maintenance.html':'MantenimientoServlet?area=mc',
    'sup_dashboard.html':'ConsultaServlet?vista=general&menu=sup&area=sup'
  };
  const form=document.querySelector('.login-form');let focusingError=false;
  const showError=(input,invalid)=>{const shell=input.closest('.login-input-shell'),message=document.getElementById(input.getAttribute('aria-describedby'));shell?.classList.toggle('has-error',invalid);input.setAttribute('aria-invalid',String(invalid));if(message)message.hidden=!invalid;};
  form.querySelectorAll('input[required]').forEach(input=>{const shell=input.closest('.login-input-shell'),message=document.getElementById(input.getAttribute('aria-describedby'));if(message){shell.querySelector('.olp-control').append(message);message.style.font=getComputedStyle(input).font;message.style.setProperty('font-size',getComputedStyle(input).fontSize,'important');message.hidden=true;}input.addEventListener('input',()=>showError(input,false));input.addEventListener('change',()=>showError(input,false));});
  form.addEventListener('submit',e=>e.preventDefault());
  for(const type of ['pointerdown','mousedown','focusin','keydown','beforeinput','paste','cut','drop','compositionstart'])form.addEventListener(type,event=>{
    if(focusingError)return;
    if(type==='keydown'&&(event.key==='Tab'||event.key==='Escape'||['Shift','Control','Alt','Meta'].includes(event.key)))return;
    const shell=event.target.closest('.login-input-shell');const input=shell?.querySelector('input');if(input)showError(input,false);
  },true);
  document.querySelector('.olp-password-toggle').addEventListener('click',e=>{
    const input=document.querySelector('#contrasena'),show=input.type==='password';
    input.type=show?'text':'password';e.currentTarget.querySelector('i').className=show?'fa-regular fa-eye-slash':'fa-regular fa-eye';
    e.currentTarget.setAttribute('aria-label',show?'Ocultar contraseña':'Mostrar contraseña');
    e.currentTarget.title=show?'Ocultar contraseña':'Mostrar contraseña';
  });
  document.querySelectorAll('[data-login-target]').forEach(button=>button.addEventListener('click',()=>{
    const target=destinations[button.dataset.loginTarget];
    const fields=[...form.querySelectorAll('input[required]')];let valid=true;fields.forEach(input=>{const empty=!input.value.trim();showError(input,empty);if(empty)valid=false;});if(!valid){focusingError=true;try{fields.find(i=>!i.value.trim())?.focus();}finally{focusingError=false;}return;}if(target)location.assign(target);
  }));
});
