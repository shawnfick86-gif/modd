package com.ollie.hack;
import com.ollie.hack.modules.combat.*; import com.ollie.hack.modules.movement.*; import com.ollie.hack.modules.render.*;
import com.ollie.hack.modules.automation.*; import com.ollie.hack.modules.exploit.*;
import java.util.ArrayList; import java.util.List;
public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();
    public static void init() {
        modules.add(new KillAura()); modules.add(new Reach()); modules.add(new Velocity()); modules.add(new Criticals());
        modules.add(new Fly()); modules.add(new NoFall()); modules.add(new Speed()); modules.add(new Scaffold());
        modules.add(new Fullbright()); modules.add(new ESP()); modules.add(new XRay());
        modules.add(new AutoTotem()); modules.add(new AutoEat());
        modules.add(new Nuker()); modules.add(new FastPlace());
    }
    public static void onTick() { for (Module m : modules) { if (m.isEnabled()) m.onTick(); } }
    public static Module getModule(String name) { for (Module m : modules) { if (m.getName().equalsIgnoreCase(name)) return m; } return null; }
}
