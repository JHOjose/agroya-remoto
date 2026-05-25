/**
 * municipios.js — Lista oficial de municipios del Huila (Colombia)
 * Fuente: DANE, 37 municipios.
 *
 * Uso en cualquier template:
 *   <script th:src="@{/js/municipios.js}"></script>
 *   llenarSelectMunicipios('idDelSelect', 'Neiva');
 *
 * Al usar un <select> con esta lista se evita el problema
 * "Neiva" vs "neiva" vs "NEIVA" en la agrupación de logística.
 * El backend ya tiene UPPER() en las queries, pero es mejor
 * prevenir en el origen.
 */

const MUNICIPIOS_HUILA = [
  "Acevedo", "Agrado", "Aipe", "Algeciras", "Altamira",
  "Baraya", "Campoalegre", "Colombia", "Elías", "Garzón",
  "Gigante", "Guadalupe", "Hobo", "Iquira", "Isnos",
  "La Argentina", "La Plata", "Nátaga", "Neiva", "Oporapa",
  "Paicol", "Palermo", "Palestina", "Pital", "Pitalito",
  "Rivera", "Saladoblanco", "San Agustín", "Santa María",
  "Suaza", "Tarqui", "Tello", "Teruel", "Tesalia",
  "Timaná", "Villavieja", "Yaguará"
];

/**
 * Llena un <select> con los municipios del Huila.
 * @param {string} selectId  — id del elemento <select>
 * @param {string} selected  — municipio a pre-seleccionar (opcional)
 * @param {boolean} addBlank — añadir opción vacía al inicio (default: true)
 */
function llenarSelectMunicipios(selectId, selected = '', addBlank = true) {
  const sel = document.getElementById(selectId);
  if (!sel) return;
  sel.innerHTML = '';
  if (addBlank) {
    const blank = document.createElement('option');
    blank.value = ''; blank.textContent = 'Selecciona un municipio...';
    sel.appendChild(blank);
  }
  MUNICIPIOS_HUILA.forEach(m => {
    const opt = document.createElement('option');
    opt.value = m; opt.textContent = m;
    if (m === selected) opt.selected = true;
    sel.appendChild(opt);
  });
}
