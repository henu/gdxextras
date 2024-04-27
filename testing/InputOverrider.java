package fi.henu.gdxextras.testing;

import fi.henu.gdxextras.ScreenStackGame;

public interface InputOverrider
{
	// Returns false if InputOverrider should be destroyed
	boolean run(ScreenStackGame screens);

	void dispose();
}
