var xhttp = new XMLHttpRequest();
var av1;
var resp;
xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
       // Typical action to be performed when the document is ready:
  		resp = JSON.parse(xhttp.responseText);
  		if (typeof avl != 'undefined') {
        var navValues =resp[resp.length-1].state.data.nav;
       document.getElementById('HKIV01NavValue').innerHTML = Math.round(parseFloat(navValues.split(",")[0]) * parseFloat(avl.split(",")[0])*100) /100;
       document.getElementById('DBKS01NavValue').innerHTML = Math.round(parseFloat(navValues.split(",")[1]) * parseFloat(avl.split(",")[1])*100) /100;
       document.getElementById('DBKS02NavValue').innerHTML = Math.round(parseFloat(navValues.split(",")[2]) * parseFloat(avl.split(",")[2])*100) /100;
       document.getElementById('LUKT01NavValue').innerHTML = Math.round(parseFloat(navValues.split(",")[3]) * parseFloat(avl.split(",")[3])*100) / 100;
       document.getElementById('navUpdateTime').innerHTML  = resp[1].state.data.date;
  		}
    }
};

var xhttp1 = new XMLHttpRequest();
xhttp1.onreadystatechange = function() {
    if (this.readyState == 4 && this.status >= 200 && this.status < 400) {
       // Typical action to be performed when the document is ready:
       avl = xhttp1.responseText;
       if (typeof resp != 'undefined') {
       var navValues =resp[resp.length-1].state.data.nav;
       document.getElementById('HKIV01NavValue').innerHTML = parseFloat(navValues.split(",")[0]) * parseFloat(avl.split(",")[0]);
       document.getElementById('DBKS01NavValue').innerHTML = parseFloat(navValues.split(",")[1]) * parseFloat(avl.split(",")[1]);
       document.getElementById('DBKS02NavValue').innerHTML = parseFloat(navValues.split(",")[2]) * parseFloat(avl.split(",")[2]);
       document.getElementById('LUKT01NavValue').innerHTML = parseFloat(navValues.split(",")[3]) * parseFloat(avl.split(",")[3]);
       document.getElementById('navUpdateTime').innerHTML = resp[1].state.data.date;
   }
    }
};
//xhttp1.open("GET", "http://52.221.244.252:10013/api/iou/total_holding", true);
xhttp1.open("GET", window.location.href.split("/web")[0]+"/api/iou/total_holding", true);
//xhttp.open("GET", "http://52.221.244.252:10007/api/iou/navvalues", true);
xhttp.open("GET", window.location.href.split(":")[0]+"://"+window.location.href.split("/")[2].split(":")[0]+":10007/api/iou/navvalues", true);
xhttp.send();
xhttp1.send();