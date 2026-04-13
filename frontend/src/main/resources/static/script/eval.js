var evalButton = document.getElementById('eval');

evalButton.addEventListener('click', evalImage);

function evalImage() {
  clearText();
  var waiting = document.getElementById('waiting');
  waiting.classList.remove('hiding');
  var mode = document.getElementById('mode');
  var data = canvas.toDataURL();
  tokens = data.split(',');
  pngImage = tokens[1].trim();

  var request = new XMLHttpRequest();
  request.onreadystatechange = function() {
    if (request.readyState == 4) {
      waiting.classList.add('hiding');
      var response = request.responseText;
      if (request.status == 200) {
        if (mode.value === 'math') {
          res = document.getElementById('result_math');
          res.innerHTML = '\\(' + response + '\\)';
          MathJax.Hub.Queue(['Typeset', MathJax.Hub, res]);
        } else {
          res = document.getElementById('result_chinese');
          res.innerHTML = response;
        }
      } else {
        error = document.getElementById('error');
        error.innerHTML = response;
      }
    }
  }
  request.open('POST', 'recognition/writing/eval', true);
  request.setRequestHeader('Content-type', 'text/plain');
  request.send('img=' + pngImage + '&mode=' + mode.value);
}

function clearText() {
  document.getElementById('result_chinese').innerHTML = '';
  document.getElementById('result_math').innerHTML = '';
  document.getElementById('error').innerHTML = '';
}
