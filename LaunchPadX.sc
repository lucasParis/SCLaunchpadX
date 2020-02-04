LaunchPadX{

	var midiIn;
	var inClient, outClient;
	var <midiRecv, <midiOut;

	var testSynths;

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

		testSynths = ();

		midiRecv.noteOn = {
			arg id, channel, note, velocity;
			var x, y;
			x = (note -11)%10;
			y = floor((note -11)/10).asInt;
			[x,y].postln;
			/*a.postln;
			b.postln;
			c.postln;
			d.postln;*/

			midiOut.noteOn(0, note, (4*velocity/127));



			testSynths[note.asSymbol] = Synth.new(\lpTest,[freq: Scale.minor.degreeToFreq(x , 60.midicps, y-3), amp: 0.4 * pow(velocity/127.0, 0.5)]);

		};
		midiRecv.noteOff = {
			arg id, channel, note, velocity;
			var x, y;
			x = (note -11)%10;
			y = floor((note -11)/10).asInt;


			midiOut.noteOff(0,note,0);
			testSynths[note.asSymbol].release;
		};


		midiRecv.polytouch = {
			arg id, channel, note, touch;

			midiOut.noteOn(0, note, (4*touch/127));


			testSynths[note.asSymbol].set(\amp, 0.4 * pow(touch/127.0, 0.5));

		};
	}

	testLeds
	{

	}
}