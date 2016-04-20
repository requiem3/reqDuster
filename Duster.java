package reqDuster;

import org.powerbot.script.Script;
import org.powerbot.script.PollingScript;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Item;
import org.powerbot.script.rt4.Bank;
import org.powerbot.script.*;
import org.powerbot.script.PaintListener;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

@Script.Manifest(name = "reqDuster", description = "turns chocolate bars into dust")
//TODO: read todos, also add for when no more gold terminate.
public class Duster extends PollingScript<ClientContext> implements PaintListener {
    private int cBar = 1973;
    private int cDust = 1975;
    private int knife = 946;
    private int dust = 0;

    @Override
    public void poll() {
        final State state = getState();
        if(state == State.DONE) {
            return;
        }

        switch(state) {
            case GRINDING:
                if(ctx.bank.opened()) { //bank is open, close that shit
                    ctx.bank.close();
                }
                else {
                    Item k = ctx.inventory.select().id(knife).poll();
                    Item b = ctx.inventory.select().id(cBar).reverse().poll();

                    int x = b.centerPoint().x - 11 + Random.nextInt(0,25);
                    int y = b.centerPoint().y - 12 + Random.nextInt(0,25);


                    k.click();

                    if(ctx.inventory.selectedItem().id() == 946) {
                        ctx.input.click(new Point(x,y), true);
                        dust++;
                    }
                }
                break;

            case BANKING:
                if(!ctx.bank.open()) {
                    ctx.bank.open();
                } else if(ctx.inventory.select().id(cDust).count() > 0) {
                    ctx.bank.deposit(cDust, Bank.Amount.ALL);
                } else if(ctx.inventory.select().id(cDust).isEmpty() && ctx.inventory.select().id(cBar).
                        isEmpty()) {
                    ctx.bank.withdraw(cBar, Bank.Amount.ALL);
                } else {
                    ctx.bank.close();
                }
                break;
        }
    }

    private State getState() {
        if(ctx.inventory.select().id(cBar).count() != 0) { //if there are still bars in inventory, grind(lol grind)
            return State.GRINDING;
        } else if(ctx.bank.opened() && ctx.bank.select().id(cBar).count() == 0) {
            return State.DONE;
        } else if(ctx.inventory.select().id(cBar).count() == 0) { //if no bars left we should bank probs
            return State.BANKING;
        }
        return State.DONE;
    }

    private enum State {
        GRINDING,BANKING,DONE
    }

    @Override
    public void repaint(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;

        final int bHr = (int) ((dust * 3600000D) / getRuntime());
        int seconds = (int) (getRuntime() / 1000) % 60 ;
        int minutes = (int) ((getRuntime() / (1000*60)) % 60);
        int hours   = (int) ((getRuntime() / (1000*60*60)) % 24);

        g.setColor(Color.BLACK);
        g.fillRect(5, 5, 145, 45);

        g.setColor(Color.GREEN);
        g.drawString(String.format("dust/hr: %,d", bHr), 10, 20);
        g.drawString(String.format("%d hour, %d min, %d sec", hours, minutes, seconds), 10, 30);
    }

}
