package spel.entity.mob;

import spel.Settings;
import spel.entity.Entity;
import spel.entity.mob.player.Player;
import spel.entity.projectile.Projectile;
import spel.graphics.Animation;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.level.Level;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Mob extends Entity {

    public double xv = 0, yv = 0;
    protected int hitboxWidth, hitboxHeight;
    protected Animation animation;
    Animation[] animations;
    public int dir = 1;
    protected int health;
    public boolean onGround = false;
    protected boolean godmode = false;
    public boolean dead = false;
    public boolean moving = false;
    protected long graceTime = 0;
    private long freezetimer = 0;
    int damage = 0;

    protected List<Projectile> projectiles = new ArrayList<>();

    @Override
    public void render(Screen screen) {
        screen.renderSprite(animation.getSprite(), (int) x , Settings.HEIGHT - (int) y - 70, dir);
    }

    @Override
    public void update() {
        for (Projectile p : projectiles) {
            p.update();
            int xp = p.getX() + p.getSprite().width / 2;
            int yp = p.getY() + 32;
            for (Mob m : level.mobs) {
                if (xp >= m.getX() && xp <= m.getX() + m.hitboxWidth) {
                    if (yp >= m.getY() && yp <= m.getY() + m.hitboxHeight) {
                        if (!m.dead) {
                            m.attackThis(this, p.damage, p.knockback, p.freezetime);
                            p.remove();
                        }
                    }
                }
            }
        }
        projectileClear();

        if (health <= 0 || dead) {
            kill();
            return;
        }

        move(xv, yv);
        if (!onGround && !godmode) {
            yv -= level.gravity;
        }
        if (!(this instanceof Player)) {
            Rectangle r1 = new Rectangle((int) x, (int) y, hitboxWidth, hitboxHeight);
            Rectangle r2 = new Rectangle(level.player.getX(), level.player.getY(), level.player.hitboxWidth, level.player.hitboxHeight);

            if (intersect(r1, r2) && !level.player.dead) {
                level.player.attackThis(this, damage, 20, 100);
            }
        }

    }

    public void kill() {
        removed = true;
    }

    protected boolean move(double xv, double yv) {
        boolean collision = false;
        if (xv > 0) {
            dir = 1;
        } else if (xv < 0) {
            dir = -1;
        }
        if (freezetimer <= System.currentTimeMillis()) {
            if (!collision((int) (x + xv), (int) y) && xv != 0) {
                x += xv;
                moving = true;
            } else {
                this.xv = 0;
                moving = false;
                collision = true;
            }
        }

        if (!collision((int) x, (int) (y + yv))) {
            y += yv;
        } else {
            if (yv < 0) {
                onGround = true;
                int yt = (int) y;
                while (!collision((int) x, yt - 1)) {
                    yt--;
                }
                y = yt;
                collision = true;
            }
            this.yv = 0.0;
        }
        if (onGround && y > 0) {
            if (!collision((int) x, (int) y - 1)) {
                onGround = false;
            } else {
                level.getTile((int) (x) / Level.tileSize, level.height - (int) (y - 1) / Level.tileSize - 1).onCollision(this, (int) (x) / Level.tileSize, (int) (y) / Level.tileSize);
                level.getTile((int) (x + hitboxWidth) / Level.tileSize, level.height - (int) (y - 1) / Level.tileSize - 1).onCollision(this, (int) (x) / Level.tileSize, (int) (y) / Level.tileSize);
            }

        }
        return collision;
    }

    @Override
    protected boolean collision(int x, int y) {
        if (y < -hitboxHeight) {
            kill();
        }
        if (x + hitboxWidth < 0) {
            return false;
        }
        if (x > level.width * Level.tileSize) {
            return false;
        }
        if (godmode) return false;

        return tileCollision(x, y);
    }

    private boolean tileCollision(int x, int y) {
        for (int yc = 0; yc < hitboxHeight; yc += 10) {
            for (int xc = 0; xc < hitboxWidth; xc += 10) {
                if (yc > hitboxHeight) yc = hitboxHeight;
                if (xc > hitboxWidth) xc = hitboxWidth;
                if (level.getTile((xc + x) / Level.tileSize, level.height - (y + yc) / Level.tileSize - 1).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void push(int units) {
        if (!collision((int) (x + units), (int) y)) {
            x += units;
        } else {
            if (units < -1) {
                push(units + 1);
            } else if (units > 1) {
                push(units - 1);
            } else {
                return;
            }
        }
    }

    public void attackThis(Mob attacker, int damage, int knockback, int freezems) {
        if (godmode) return;
        freezetimer = level.time + freezems;
        push(knockback * attacker.dir);
        health -= damage;
        graceTime = level.time + 2000;
    }

    public void setHp(int hp) {
        health = hp;
    }

    protected boolean isProtected() {
        return graceTime > level.time;
    }

    protected void setHitbox(int width, int height) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
    }

    protected boolean setAnimation(Animation anim, int time) {
        if (!animation.locked && animation != anim) {
            animation = anim;
            animation.start(time);
            return true;
        }
        return false;
    }

    public Sprite getSprite() {
        if (animation == null) return Sprite.err;
        return animation.getSprite();
    }

    /**
     * Remove the expired & collided projectiles
     */
    private void projectileClear() {
        for (int i = 0; i < projectiles.size(); i++) {
            if (projectiles.get(i).isRemoved()) {
                projectiles.remove(i);
            }
        }
    }

    public Mob clone() {
        return (Mob) super.clone();
    }
}
