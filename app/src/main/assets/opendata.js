
var database = firebase.database();
var audio = new Audio('assets/success.m4a');

// Implementation of AR Exploration mode
var World = {

	//  the last best known location
	userLocation: null,

    // the user's unique ID to reference firebase with
	firebaseUid: null,

	// make sure data is only loaded once
	isRequestingData: false,

	initiallyLoadedData: false,

	// poi not selected, poi selected, direction arrow
	markerDrawable_idle: null,
	markerDrawable_selected: null,
	markerDrawable_directionIndicator: null,

	// active AR objects
	markerList: [],

	// current active marker
	currentMarker: null,

	locationUpdateCounter: 0,
	updatePlacemarkDistancesEveryXLocationUpdates: 10,

	// gets data from java - firebase UID

    getDataFromNative: function getDataFromNativeFn(data){
        //console.log("get data method");

        var obj = JSON.parse(data);
        console.log(obj.destination[0].uid);
        World.firebaseUid = obj.destination[0].uid;

        console.log(World.firebaseUid);
    },

	loadPoisFromJsonData: function loadPoisFromJsonDataFn(poiData) {

	    //World.getDataFromNative();

		AR.context.destroyAll();



		// inflate the radar
		PoiRadar.show();
		$('#radarContainer').unbind('click');
		$("#radarContainer").click(PoiRadar.clickedRadar);

		// no markers initially
		World.markerList = [];

		// find paths of marker drawables
		World.markerDrawable_idle = new AR.ImageResource("assets/marker_notactive.png");
		World.markerDrawable_selected = new AR.ImageResource("assets/marker_active.png");
		World.markerDrawable_directionIndicator = new AR.ImageResource("assets/indi.png");

		// loop through POI-data and create AR object for each POI
		for (var currentPlaceNr = 0; currentPlaceNr < poiData.length; currentPlaceNr++) {
			var singlePoi = {
				"id": poiData[currentPlaceNr].id,
				"latitude": parseFloat(poiData[currentPlaceNr].latitude),
				"longitude": parseFloat(poiData[currentPlaceNr].longitude),
				"altitude": parseFloat(poiData[currentPlaceNr].altitude),
				"title": poiData[currentPlaceNr].name,
				"description": poiData[currentPlaceNr].description,
				"trophyRendered" : "false"
			};

			World.markerList.push(new Marker(singlePoi));
		}

		// updates distance information of all placemarks
		World.updateDistanceToUserValues();

		World.updateStatusMessage(currentPlaceNr + ' places loaded');

		// set distance slider to 100%
		$("#panel-distance-range").val(100);
		$("#panel-distance-range").slider("refresh");
	},

	// update distance to markers so calculation is not needed every time
	updateDistanceToUserValues: function updateDistanceToUserValuesFn() {
		for (var i = 0; i < World.markerList.length; i++) {
			World.markerList[i].distanceToUser = World.markerList[i].markerObject.locations[0].distanceToUser();
		}
	},

	// status message bottom of screen
	updateStatusMessage: function updateStatusMessageFn(message, isWarning) {

		var themeToUse = isWarning ? "e" : "c";
		var iconToUse = isWarning ? "alert" : "info";

		$("#status-message").html(message);
		$("#popupInfoButton").buttonMarkup({
			theme: themeToUse
		});
		$("#popupInfoButton").buttonMarkup({
			icon: iconToUse
		});
	},


	// user clicked "More" button in POI-detail panel -> fire event to open native screen
	onPoiDetailMoreButtonClicked: function onPoiDetailMoreButtonClickedFn() {
		var currentMarker = World.currentMarker;
		var markerSelectedJSON = {
            action: "present_poi_details",
            id: currentMarker.poiData.id,
            title: currentMarker.poiData.title,
            description: currentMarker.poiData.description
        };

		/*
			The sendJSONObject method can be used to send data from javascript to the native code.
		*/
		//AR.platform.sendJSONObject(markerSelectedJSON);

        World.currentMarker.setDeselected(World.currentMarker);

        $("#panel-poidetail").panel("close", 123);


        //add to favourites here

        var userRef = database.ref('users/' + World.firebaseUid + '/');

        userRef.push(currentMarker.poiData.title);


        World.updateStatusMessage("Added " + currentMarker.poiData.title + " to favourites");

	},

    onPoiDetailMoreButtonClickedInfo: function onPoiDetailMoreButtonClickedInfoFn() {
        var currentMarker = World.currentMarker;
        var markerSelectedJSON = {
            action: "present_poi_details",
            id: currentMarker.poiData.id,
            title: currentMarker.poiData.title,
            description: currentMarker.poiData.description
        };

        AR.platform.sendJSONObject(markerSelectedJSON);

        //window.open('htttp://google.com', 'external','width=500, height=400');



    },

	// location updates called from custom LocationProdiver java class
	locationChanged: function locationChangedFn(lat, lon, alt, acc) {

		// save user location so we can call it from anywhere
		World.userLocation = {
			'latitude': lat,
			'longitude': lon,
			'altitude': alt,
			'accuracy': acc
		};


		console.log('Location changed');
		console.log(lat);
		console.log(lon);
		console.log(acc);
		console.log(World.markerList);

		for(var x =0; x<World.markerList.length; x++){

            if (World.markerList[x].distanceToUser <= 50){
                console.log('MADE IT TO' + World.markerList[x].poiData.title);

                if (World.markerList[x].poiData.trophyRendered = "false"){

                    var random = Math.floor(Math.random() * 3) + 1;

                    //render the trophy

                    //var location = new AR.GeoLocation(parseFloat(lat), parseFloat(lon), parseFloat(alt));

                    var location = new AR.RelativeLocation(null, 5, 0, 0);

                    var modelGift = new AR.Model("assets/cube.wt3", {
                        //onLoaded: this.worldLoaded,
                        scale: {
                            x: 0.5,
                            y: 0.5,
                            z: 0.5
                        },
                        onClick : function(arObject) {


                            path = "assets/coupon" + random + ".png";

                            document.getElementById("prizeImage").style.display = 'block';

                            $("#prizeImage").attr("src", path);

                            arObject.enabled = false;
                            audio.play();
                            //alert('here is your reward');
                          }


                    });


                    var obj = new AR.GeoObject(location, {
                        drawables: {
                           cam: [modelGift]
                        }
                    });

                    audio.play();

                    World.markerList[x].poiData.trophyRendered = "true";


                }









            }

		}


		// request data if not loaded
		if (!World.initiallyLoadedData) {
			World.requestDataFromServer(lat, lon);
			World.initiallyLoadedData = true;
		} else if (World.locationUpdateCounter === 0) {

			World.updateDistanceToUserValues();
		}


		World.locationUpdateCounter = (++World.locationUpdateCounter % World.updatePlacemarkDistancesEveryXLocationUpdates);
	},

	// click listener for AR markers
	onMarkerSelected: function onMarkerSelectedFn(marker) {
		World.currentMarker = marker;

		// update panel values
		$("#poi-detail-title").html(marker.poiData.title);
		$("#poi-detail-description").html(marker.poiData.description);

		if( undefined == marker.distanceToUser ) {
			marker.distanceToUser = marker.markerObject.locations[0].distanceToUser();
		}
		var distanceToUserValue = (marker.distanceToUser > 999) ? ((marker.distanceToUser / 1000).toFixed(2) + " km") : (Math.round(marker.distanceToUser) + " m");

		$("#poi-detail-distance").html(distanceToUserValue);

		// show panel
		$("#panel-poidetail").panel("open", 123);

		$(".ui-panel-dismiss").unbind("mousedown");

		$("#panel-poidetail").on("panelbeforeclose", function(event, ui) {
			World.currentMarker.setDeselected(World.currentMarker);
		});
	},

	// missed AR object with click - do nothing
	onScreenClick: function onScreenClickFn() {

	},

	// returns distance in meters of furthest POI from the user
	getMaxDistance: function getMaxDistanceFn() {

		// sort places by descneding distance = first = MAX
		World.markerList.sort(World.sortByDistanceSortingDescending);

		// use distanceToUser to get max-distance
		var maxDistanceMeters = World.markerList[0].distanceToUser;


		return maxDistanceMeters * 1.1;
	},

	// update parameters shown in the sidebar
	updateRangeValues: function updateRangeValuesFn() {

		// slider value set by the user
		var slider_value = $("#panel-distance-range").val();

		// max range relative to the maximum distance of all visible places
		var maxRangeMeters = Math.round(World.getMaxDistance() * (slider_value / 100));

		// range in meters
		var maxRangeValue = (maxRangeMeters > 999) ? ((maxRangeMeters / 1000).toFixed(2) + " km") : (Math.round(maxRangeMeters) + " m");

		// number of places within max-range
		var placesInRange = World.getNumberOfVisiblePlacesInRange(maxRangeMeters);

		// update UI labels accordingly
		$("#panel-distance-value").html(maxRangeValue);
		$("#panel-distance-places").html((placesInRange != 1) ? (placesInRange + " Places") : (placesInRange + " Place"));

		// only render places within the range
		AR.context.scene.cullingDistance = Math.max(maxRangeMeters, 1);

		// update radar's distance - user position updated too
		PoiRadar.setMaxDistance(Math.max(maxRangeMeters, 1));
	},

	// how many places fall within the set range
	getNumberOfVisiblePlacesInRange: function getNumberOfVisiblePlacesInRangeFn(maxRangeMeters) {

		// sort markers by distance
		World.markerList.sort(World.sortByDistanceSorting);

		// when a place found out of the range, then stop the loop
		for (var i = 0; i < World.markerList.length; i++) {
			if (World.markerList[i].distanceToUser > maxRangeMeters) {
				return i;
			}
		};

	    // if loop doesnt break -  all are withing range
		return World.markerList.length;
	},

	handlePanelMovements: function handlePanelMovementsFn() {

		$("#panel-distance").on("panelclose", function(event, ui) {
			$("#radarContainer").addClass("radarContainer_left");
			$("#radarContainer").removeClass("radarContainer_right");
			PoiRadar.updatePosition();
		});

		$("#panel-distance").on("panelopen", function(event, ui) {
			$("#radarContainer").removeClass("radarContainer_left");
			$("#radarContainer").addClass("radarContainer_right");
			PoiRadar.updatePosition();
		});
	},

	// display range slider
	showRange: function showRangeFn() {
		if (World.markerList.length > 0) {

			// update labels on every range movement
			$('#panel-distance-range').change(function() {
				World.updateRangeValues();
			});

			World.updateRangeValues();
			World.handlePanelMovements();

			// open panel
			$("#panel-distance").trigger("updatelayout");
			$("#panel-distance").panel("open", 1234);
		} else {

			// no places are visible, because the are not loaded yet
			World.updateStatusMessage('No places available yet', true);
		}
	},

	// try and load POI data again incase of any errors or internet delay
	reloadPlaces: function reloadPlacesFn() {
		if (!World.isRequestingData) {
			if (World.userLocation) {
				World.requestDataFromServer(World.userLocation.latitude, World.userLocation.longitude);
			} else {
				World.updateStatusMessage('Unknown user-location.', true);
			}
		} else {
			World.updateStatusMessage('Already requesing places...', true);
		}
	},

	// request POI data
	requestDataFromServer: function requestDataFromServerFn(lat, lon) {

		// desribed in the top of the file
		World.isRequestingData = true;
		World.updateStatusMessage('Requesting places from web-service');

		// read the firebase database
        var serverUrl = "https://explorar-fyp.firebaseio.com/poilist.json";
		var jqxhr = $.getJSON(serverUrl, function(data) {
				World.loadPoisFromJsonData(data);
			})
			.error(function(err) {
				World.updateStatusMessage("Invalid web-service response.", true);
				World.isRequestingData = false;
			})
			.complete(function() {
				World.isRequestingData = false;
			});
	},

	// helper to sort places by distance
	sortByDistanceSorting: function(a, b) {
		return a.distanceToUser - b.distanceToUser;
	},

	// helper to sort places by distance, descending
	sortByDistanceSortingDescending: function(a, b) {
		return b.distanceToUser - a.distanceToUser;
	}

};


/* forward locationChanges to custom function */
AR.context.onLocationChanged = World.locationChanged;

/* forward clicks in empty area to World */
AR.context.onScreenClick = World.onScreenClick;