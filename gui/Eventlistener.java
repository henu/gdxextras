package fi.henu.gdxextras.gui;

public interface Eventlistener
{
	// Returns true if the event was processed, as in InputProcessor events.
	boolean handleGuiEvent(GuiEvent event);
}
