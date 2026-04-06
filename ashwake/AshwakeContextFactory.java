package ashwake;

import core.Context;
import core.Entity;
import core.Relation;

final class AshwakeContextFactory {

    private AshwakeContextFactory() {
    }

    public static Context create(AshwakeRunWorld world) {
        Context context = new Context();
        context.setState(world);
        registerEntity(context, world);
        return context;
    }

    private static void registerEntity(Context context, Entity<? extends Relation> entity) {
        context.getEntities().add(entity);
        context.getGraph().addEntity(entity.getId(), entity.getLabel());

        for (Relation child : entity.getChildren()) {
            if (child instanceof Entity<?> childEntity) {
                registerEntity(context, childEntity);
                context.getGraph().addRelation(
                    entity.getId(),
                    entity.getLabel(),
                    childEntity.getId(),
                    childEntity.getLabel()
                );
            } else {
                context.getEntities().add(child);
            }
        }
    }
}
