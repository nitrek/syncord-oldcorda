

function navValue() {
var x = Math.floor((Math.random() * (12-8)+8)*100)/100;
document.getElementById("HKIV01NavValue").innerHTML = x;
var x = Math.floor((Math.random() * (100-98)+98)*100)/100;
document.getElementById("DBKS01NavValue").innerHTML = x;
var x = Math.floor((Math.random() * (51-49)+49)*100)/100;
document.getElementById("DBKS02NavValue").innerHTML = x;
var x = Math.floor((Math.random() * (26-24)+24)*100)/100;
document.getElementById("LUKT01NavValue").innerHTML = x;

}
var interval = parseInt(document.getElementById("autorefresh").value)
var setIntervalTimer = setInterval(navValue, interval);


function select_autoRefresh() {	
	clearInterval(setIntervalTimer)
	var interval = parseInt(document.getElementById("autorefresh").value)
	setIntervalTimer = setInterval(navValue,interval);

}

navValue()