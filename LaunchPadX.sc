LaunchPadX{

	var midiIn;
	var inClient, outClient;
	var <midiRecv, <midiOut;

	var <> onPadDown, <> onPadUp, <> onPressure;

	*new{
		^super.new.init();
	}

	/*getColor{
		arg x,y;
		var color, degree;
		// degree = this.getDegree(x,y);
		color = 0;
		if((degree%7) == 0)
		{
			color = 39;
		};
		if((degree%7) == 4)
		{
			color = 11;
		};
		^color;
	}*/

	init{
		inClient = MIDIClient.sources.detect{|a|a.name.contains("LPX MIDI Out")};
		outClient = MIDIClient.destinations.detect{|a|a.name.contains("LPX MIDI In")};

		midiRecv = MIDIIn.connect(0, inClient.uid);
		midiOut = MIDIOut(0, outClient.uid);
		midiOut.latency = 0;
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 0, 127, 247]);//enable programmer mode
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 11, 0, 0/*sensivity*/, 247]);//enable polytouch with sensitive setting
		midiOut.sysex(Int8Array[240, 0, 32, 41, 2, 12, 4, 2/*sensivity*/, 0, 247]);//velocity curve

		/*//initial color
		8.do{
			arg x;
			8.do{
				arg y;
				var color, note;
				color = this.getColor(x,y);

				note = this.xyToNote(x,y);

				this.setColor(x, y, color);
			};
		};*/

		midiRecv.noteOn = {
			arg id, channel, note, velocity;
			var xy;
			xy  = this.noteToXY(note);

			if(this.onPadDown != nil)
			{
				this.onPadDown.(xy.x, xy.y, velocity, this);
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
				this.onPressure.(xy.x, xy.y, touch, this);
			};
		};
	}

	setColor{
		arg x, y, color;
		var note;
		note = this.xyToNote(x,y);
		midiOut.noteOn(0, note, color);
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
		y = floor((note -11)/10).asInt;
		^(\x:x,\y:y);
	}
}

//set sysex colors