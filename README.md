# Vending Machine - Java Swing GUI

This project contains only the graphical interface for the vending-machine project.

## Structure
- `src/vending/gui/Main.java` - application entry point.
- `src/vending/gui/MainFrame.java` - main Swing window.
- `src/vending/gui/VendingController.java` - integration contract for the team's automaton/parser/backend.
- `src/vending/gui/ProductView.java` - display-only product data.
- `src/vending/gui/VendingViewModel.java` - data sent by the backend to refresh the GUI.

## Team integration
The GUI does NOT implement the automaton, lexer, parser, semantic validation, or JSON logic.

The rest of the team can implement `VendingController` and connect it to `MainFrame`.

The current `Main.java` uses a small demo controller so the GUI can be tested before the real backend is connected.
