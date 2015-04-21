function printListItem(parentDiv,name,info,url,identity,stars){
	var listItem = document.createElement("div");
	listItem.setAttribute("name","list-item");

	var p1 = document.createElement("p");
	var font11 = document.createElement("font");
	font11.setAttribute("name","name");
	var a11 = document.createElement("a");
	a11.setAttribute("href",url);
	node11 = document.createTextNode(name);
	a11.appendChild(node11);
	font11.appendChild(a11);
	p1.appendChild(font11);
	var font12 = document.createElement("font");
	font12.setAttribute("name","identity");
	var node12 = document.createTextNode("微信号:"+identity);
	font12.appendChild(node12);
	p1.appendChild(font12);
	listItem.appendChild(p1);

	var p2 = document.createElement("p");
	var font21 =document.createElement("font");
	font21.setAttribute("name","info");
	var node21 = document.createTextNode(info);
	font21.appendChild(node21);
	p2.appendChild(font21);
	listItem.appendChild(p2);

	var div3 = document.createElement("div");
	div3.setAttribute("name","stars");
	var font31 = document.createElement("font");
	font31.setAttribute("class","text-muted");
	font31.setAttribute("name","starsFont");
	var node31 = document.createTextNode("相关度:");
	font31.appendChild(node31);
	div3.appendChild(font31);
	for(var i=stars;i>0;i-=2){
		if(i > 1){
			var tempSpan = document.createElement("span");
			tempSpan.setAttribute("class","glyphicon glyphicon-star");
			div3.appendChild(tempSpan);
		}
		if(i == 1){
			var tempSpan = document.createElement("span");
			tempSpan.setAttribute("class","glyphicon glyphicon-star-empty");
			div3.appendChild(tempSpan);
		}
	}
	listItem.appendChild(div3);

	parentDiv.appendChild(listItem);
}


function setContent(div,jsonList){
	var length = jsonList.length;
	for(var i=0;i<length;i++){
		var name = jsonList[i].Name;
		var info = jsonList[i].Info;
		var url = jsonList[i].Url
		var identity = jsonList[i].Identity;
		var stars = jsonList[i].Stars;
		printListItem(div,name,info,url,identity,stars);
	}
}

function callback(jsonList){
	var div = document.getElementById("list");
	div.innerHTML = "";
	setContent(div,jsonList);
}