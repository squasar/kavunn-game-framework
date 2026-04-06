import core.Context;
import core.Entity;
import core.Relation;

public final class GameContextFactory {

    private GameContextFactory() {
    }

    public static Context create(Entity<? extends Relation> world) {
        Context context = new Context();
        registerEntity(context, world);

        for (Relation child : world.getChildren()) {
            if (child instanceof Entity<?> childEntity) {
                registerEntity(context, childEntity);
                context.getGraph().addRelation(
                    world.getId(),
                    world.getLabel(),
                    childEntity.getId(),
                    childEntity.getLabel()
                );
            } else {
                context.getEntities().add(child);
            }
        }

        return context;
    }

    private static void registerEntity(Context context, Entity<?> entity) {
        context.getEntities().add(entity);
        context.getGraph().addEntity(entity.getId(), entity.getLabel());
    }
}
