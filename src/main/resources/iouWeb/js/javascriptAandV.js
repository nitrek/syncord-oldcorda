

function navValue() {
var x = Math.round((Math.random() * 1000) + 1,2)/100;
document.getElementById("HKIV01NavValue").innerHTML = x;
var x = Math.round((Math.random() * 10000) + 4,2)/100;
document.getElementById("DBKS01NavValue").innerHTML = x;
var x = Math.round((Math.random() * 1000) + 6,2)/100;
document.getElementById("DBKS02NavValue").innerHTML = x;
var x = Math.round((Math.random() * 100) + 1,2)/100;
document.getElementById("LUKT01NavValue").innerHTML = x;

}
var interval = parseInt(document.getElementById("autorefresh").value)
var setIntervalTimer = setInterval(function(){ navValue() }, interval);


function select_autoRefresh() {	
	clearInterval(setIntervalTimer)
	var interval = parseInt(document.getElementById("autorefresh").value)
	setIntervalTimer = setInterval(function() {navValue()},interval);

}

navValue()