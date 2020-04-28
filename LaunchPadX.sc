

LPXDisplayLayer{
	var <> colorMatrix;

	*new{
		^super.new.init();
	}

	init{
		colorMatrix = Array2D.fromArray(8,8, 0!64);
	}


	setColor{
		arg x, y, color;
		colorMatrix[x, y] = color;
	}
}


LaunchPadX{
	var midiIn;
	var inClient, outClient;
	var <midiRecv, <midiOut;

	var <> onPadDown, <> onPadUp, <> onPressure;
	var <> onTopRowDown, <> onTopRowUp, <> onRightColumnDown, <> onRightColumnUp;

	var displayRoutine;
	var <> displayRefreshFlag;

	var displayLayers;
	var displayLayerOrder;

	*new{
		^super.new.init();
	}

	init{
		inClient = MIDIClient.sources.detect{|a|a.name.contains("LPX MIDI Out")};
		outClient = MIDIClient.destinations.detect{|a|a.name.contains("LPX MIDI In")};

		midiRecv = MIDIIn.connect(0, inClient.uid);
		midiOut = MIDIOut(0, outClient.uid);
		midiOut.latency = 0;
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 0, 127, 247]);//enable programmer mode
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 11, 0, 0/*sensivity*/, 247]);//enable polytouch with sensitive setting
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 4, 2/*sensivity*/, 0, 247]);//velocity curve


		midiRecv.control = {
			arg id, channel, cc, value;
			// var xy;
			// xy  = this.noteToXY(note);
			if(cc < 90)
			{
				var index;
				index = (((cc - 9)/10) -1).floor.asInteger;
				if(value > 1) {
					if(this.onRightColumnDown != nil) { this.onRightColumnDown.(index, this) };
				} {
					if(this.onRightColumnUp != nil) { this.onRightColumnUp.(index, this) };
				};
				// this.onPadUp.(xy.x, xy.y, this);
			} {
				var index;
				index = cc - 91;
				if(value > 1) {
					if(this.onTopRowDown != nil) { this.onTopRowDown.(index, this) };
				} {
					if(this.onTopRowUp != nil) { this.onTopRowUp.(index, this) };
				};
			};



		};


		midiRecv.noteOn = {
			arg id, channel, note, velocity;
			var xy;
			xy  = this.noteToXY(note);

			if(this.onPadDown != nil)
			{
				this.onPadDown.(xy.x, xy.y, velocity/127.0, this);
			};
		};



		midiRecv.noteOff = {
			arg id, channel, note, velocity;
			var xy;
			xy  = this.noteToXY(note);

			if(this.onPadUp != nil)
			{
				this.onPadUp.(xy.x, xy.y, this);
			};
		};


		midiRecv.polytouch = {
			arg id, channel, note, touch;
			var xy;
			xy  = this.noteToXY(note);

			if(this.onPressure != nil)
			{
				this.onPressure.(xy.x, xy.y, touch/127.0, this);
			};
		};

		displayLayerOrder = ();
		displayLayers = ();

		displayRefreshFlag = false;
		//routine to display
		displayRoutine = Routine(
			{
				loop{
					displayRefreshFlag.if{
						this.display;
						displayRefreshFlag = false;

					};
					0.016.wait;
				};
			}
		).play;

	}

	display{

		//get all layers
		//traverse from highest priority, if not zero use this value
		var outputArray;
		outputArray = Array2D.fromArray(8,8,0!64);

		8.do{
			arg x;
			8.do{
				arg y;
				block{ |break|
					displayLayerOrder.size.do{
						arg i;
						var val;
						val = displayLayers[displayLayerOrder[i]][x,y];
						(val != 0).if
						(
							{
								outputArray[x,y] = val;
								break.value;
							},
							{
								outputArray[x,y] = val;
							}
						);
					};
				};
			};
		};

		//output
		8.do{
			arg x;
			8.do{
				arg y;
				var val;
				val = outputArray[x,y];
				this.setColor(x,y,val);
			};
		};

	}

	newDisplayLayer{
		arg name;
		//store name as index
		displayLayerOrder[displayLayers.size] = name.asSymbol;
		//create a new layer
		// displayLayers[name.asSymbol] = LPXDisplayLayer();
		displayLayers[name.asSymbol] = Array2D.fromArray(8,8, 0!64);


	}

	setColor{
		arg x, y, color;
		var note;
		note = this.xyToNote(x,y);
		midiOut.noteOn(0, note, color);
	}

	setLayerColor{
		arg layer, x, y, color;

		// displayLayers[layer].setColor(x, y, color);
		displayLayers[layer][x,y] = color;
		displayRefreshFlag = true;
		// var note;
		// note = this.xyToNote(x,y);
		// midiOut.noteOn(0, note, color);
	}

	xyToNote{
		arg x, y;
		var note;
		note = x + (y*10) + 11;
		^note;
	}

	noteToXY{
		arg note;
		var x, y;
		x = (note -11)%10;
		y = floor((note -11)/10).asInteger;
		^(\x:x,\y:y);
	}
}

//set sysex colors