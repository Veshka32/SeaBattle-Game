/* Implements SeaBattle game, Player vs Computer.
Player can place ships manually or automatically (random position).
Computer maintains probabilistic effective strategy of making shots while biggest ship (of length 4) is not destroyed.
Once computer shot one of the ships, it tries to end up with this ship, and only after this continue to make shots either in order of strategy or random.
Number of ships and its length are fix, but probably (not checked) are able to be reset in GameConstant Class

What I've learnt:
- ActionPerformed, ActionListeners, the different way you can add them (separate class implemets listener, distinct eventCode, anonymus listener);
- Interfaces;
- transfer event from class where it happens to where it is proceeded;
- Jframe and other simple Swing components;
- overriding paintComponent method (one of the way to draw object in JPanel);
- enum class and switch between state;
- inheritance;
- implement kind of randomQueue (quite ugly);
- avoiding "magic numbers" by setting constant values;

*/