function redistrict(){
	if(geojsonStateData != null){
    	var socket = new SockJS('/redistrict');
    	ws = Stomp.over(socket);
    	ws.connect({}, function(frame) {
    		ws.subscribe("/redistrict/errors", function(message) {
    			alert("Error " + message.body);
    		});

    		ws.subscribe("/redistrict/reply", function(message) {
                var json = JSON.parse(message.body); 
                var movedPrecinct = json['movedPrecincts'];
                setPrecinctDistrict(movedPrecinct['precinctId'], movedPrecinct['districtId']);
                redraw();
    		});
    	}, function(error) { 
    		alert("STOMP error " + error);	
    	});
    }
}

function start(){
	if (ws != null) {
    	ws.send('/app/redistrict', {}, JSON.stringify({
    		"request": "start", 
    		"stateId": stateId, 
    		"weights": [$('#compactness').val(), $('#population').val(), $('#partisan').val()],
    		"contiguity": $('input.contiguity').is(':checked')
    	}));
    	$(this).hide();
    	$('#pause').show();
    	$('#resume').show();
    	$('#stop').show();
	}
}

function pause(){
	if (ws != null) {
    	ws.send('/app/redistrict', {}, JSON.stringify({"request": 'pause'}));
    }
}

function resume(){
	if (ws != null) {
    	ws.send('/app/redistrict', {}, JSON.stringify({"request": 'resume'}));
    }
}

function stop(){
	if (ws != null) {
    	ws.send('/app/redistrict', {}, JSON.stringify({"request": 'stop'}));
    	$('#generate').hide();
    	$('#pause').hide();
    	$('#resume').hide();
    	$(this).hide();
	}
}

function disconnect(){
	if (ws != null){
		ws.disconnect();
	}
}
function compareDistricts(){
	if (ws != null) {
    	ws.send('/app/redistrict', {}, JSON.stringify({"request": 'compare'}));
		}
}