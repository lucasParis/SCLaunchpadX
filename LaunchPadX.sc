LaunchPadX{

	var midiIn;
	var inClient, outClient;
	var <midiRecv, <midiOut;

	var testSynths;

	*new{
		^super.new.init();
	}

	getColor{
		arg x,y;
		var color, degree;
		degree = this.getDegree(x,y);
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
	}

	getDegree{
		arg x, y;
		^x + (y * 2);
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

		testSynths = ();


		8.do{
			arg x;
			8.do{
				arg y;
				var color, note;
				color = this.getColor(x,y);
				// if((x == 0) || (x == 2))
				// {
				// 	color = 37;
				// };
				note = x + (y*10) + 11;

				midiOut.noteOn(0,note,color);

			};
		};




		midiRecv.noteOn = {
			arg id, channel, note, velocity;
			var x, y;
			var degree;
			x = (note -11)%10;
			y = floor((note -11)/10).asInt;

			midiOut.noteOn(0, note, (3*(velocity/127))+ 1);


			degree = this.getDegree(x,y);
			testSynths[note.asSymbol] = Synth.new(\lpTest,[freq: Scale.major.degreeToFreq(degree, 60.midicps, -1), amp: 0.4, velocity:velocity/127.0]);

		};



		midiRecv.noteOff = {
			arg id, channel, note, velocity;
			var x, y;
			var color;
			x = (note -11)%10;
			y = floor((note -11)/10).asInt;

			/*			color = 0;

			if((x == 0) || (x == 2))
			{
			color = 37;
			};*/
			color = this.getColor(x,y);

			midiOut.noteOn(0,note,color);
			testSynths[note.asSymbol].release;
		};


		midiRecv.polytouch = {
			arg id, channel, note, touch;
			// touch.postln;

			midiOut.noteOn(0, note, (3*(touch/127)) + 1);


			testSynths[note.asSymbol].set(\pressure, touch/127.0);

		};
	}

	testLeds
	{

	}
}