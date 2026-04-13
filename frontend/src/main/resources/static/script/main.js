// Taken from https://www.youtube.com/watch?v=m4sioSqlXhQ

var canvas = document.getElementById('canvas');
var context = canvas.getContext('2d');

var radius = 10;
var dragging = false;

canvas.width = 2 * window.innerWidth / 3;
canvas.height = 2 * window.innerHeight / 3;

context.lineWidth = radius * 2;

var putPoint = function(e) {
  if (dragging) {
    context.lineTo(e.clientX, e.clientY);
    context.stroke();
    context.beginPath();
    context.arc(e.offsetX, e.offsetY, radius, 0, Math.PI*2);
    context.fill();
    context.beginPath();
    context.moveTo(e.clientX, e.clientY);
  }
}

var engage = function(e) {
  dragging = true;
  putPoint(e);
}

var disengage = function() {
  dragging = false;
  context.beginPath();
}

canvas.addEventListener('mousedown', engage);
canvas.addEventListener('mousemove', putPoint);
canvas.addEventListener('mouseup', disengage);
