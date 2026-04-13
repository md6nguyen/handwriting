// Taken from https://www.youtube.com/watch?v=oMCI3p-7EHU

var setRadius = function(newRadius) {
  if (newRadius < minRad)
    newRadius = minRad;
  else if (newRadius > maxRad) 
    newRadius = maxRad;
  if (newRadius > 5 && newRadius < 6) {
    newRadius = Math.floor(newRadius);
  }
  radius = newRadius;
  context.lineWidth = radius * 2;

  radSpan.innerHTML = radius; 
}

var minRad = 0.5,
    maxRad = 100,
    defaultRad = 5,
    interval = 5,
    radSpan = document.getElementById('radval'),
    decRad = document.getElementById('decrad'),
    incRad = document.getElementById('incrad');

decRad.addEventListener('click', function() {
  setRadius(radius-interval);
});
incRad.addEventListener('click', function() {
  setRadius(radius+interval);
});

setRadius(defaultRad);
