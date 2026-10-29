document.addEventListener('DOMContentLoaded',()=>{
 const country=document.querySelector('#paisId'),region=document.querySelector('#registro-form [name=telefonoRegion]'),phone=document.querySelector('#registro-form [name=telefono]');
 if(country&&region&&phone){
  let manual=Boolean(phone.value.trim()),setting=false;
  region.addEventListener('change',()=>{if(!setting)manual=true;});
  phone.addEventListener('input',()=>{manual=true;});
  country.addEventListener('change',()=>{
   if(manual||phone.value.trim())return;
   const code=country.selectedOptions[0]?.textContent.match(/^([A-Z]{2}) · /)?.[1];
   region.value=[...region.options].some(o=>o.value===code)?code:'';
   setting=true;region.dispatchEvent(new Event('change',{bubbles:true}));setting=false;
   window.OceanLinkControls.refreshSelect(region);
  });
 }
});
