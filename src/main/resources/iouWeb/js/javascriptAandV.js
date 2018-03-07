

function navValue() {
var oldx = document.getElementById("HKIV01NavValue").innerHTML;
var x = Math.floor((Math.random() * (12-8)+8)*100)/100;
document.getElementById("HKIV01NavValue").innerHTML = x;
if(parseInt(oldx)- parseInt(x) > 0)
{
document.getElementById("HKIV01NavValue").style="background-color:red;color:white"
}
else{
document.getElementById("HKIV01NavValue").style="background-color:green;color:white"
}
var oldx = document.getElementById("DBKS01NavValue").innerHTML;
var x = Math.floor((Math.random() * (100-98)+98)*100)/100;
document.getElementById("DBKS01NavValue").innerHTML = x;
if(parseInt(oldx)- parseInt(x) > 0)
{
document.getElementById("DBKS01NavValue").style="background-color:red;color:white"
}
else{
document.getElementById("DBKS01NavValue").style="background-color:green;color:white"
}
var oldx = document.getElementById("DBKS02NavValue").innerHTML;
var x = Math.floor((Math.random() * (51-49)+49)*100)/100;
document.getElementById("DBKS02NavValue").innerHTML = x;
if(parseInt(oldx)- parseInt(x) > 0)
{
document.getElementById("DBKS02NavValue").style="background-color:red;color:white"
}
else{
document.getElementById("DBKS02NavValue").style="background-color:green;color:white"
}
var oldx = document.getElementById("LUKT01NavValue").innerHTML;
var x = Math.floor((Math.random() * (26-24)+24)*100)/100;
document.getElementById("LUKT01NavValue").innerHTML = x;
if(parseInt(oldx)- parseInt(x) > 0)
{
document.getElementById("LUKT01NavValue").style="background-color:red;color:white"
}
else{
document.getElementById("LUKT01NavValue").style="background-color:green;color:white"
}

}
var interval = parseInt(document.getElementById("autorefresh").value)
var setIntervalTimer = setInterval(navValue, interval);


function select_autoRefresh() {	
	clearInterval(setIntervalTimer)
	var interval = parseInt(document.getElementById("autorefresh").value)
	setIntervalTimer = setInterval(navValue,interval);

}

navValue()