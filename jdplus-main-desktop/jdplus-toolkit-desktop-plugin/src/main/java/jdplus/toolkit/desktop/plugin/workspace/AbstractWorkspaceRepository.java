/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.desktop.plugin.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 *
 * @author Jean Palate
 * @since 1.0.0
 */
public abstract class AbstractWorkspaceRepository implements WorkspaceRepository {

    private final Map<Class, List<WorkspaceItemRepository>> map_ = new HashMap<>();

    public <D> void register(Class<D> dclass, WorkspaceItemRepository<D> repo) {
        map_.computeIfAbsent(dclass, o -> new ArrayList<>()).add(repo);
    }

    public <D> void unregister(Class<D> dclass) {
        map_.remove(dclass);
    }

    @Nullable
    public List<WorkspaceItemRepository> getRepositories(WorkspaceItem<?> item) {
        Class<?> mclass = WorkspaceFactory.getInstance().getManager(item.getFamily()).getItemClass();
        return getRepositories(mclass);
    }

    @Nullable
    public List<WorkspaceItemRepository> getRepositories(Class dclass) {
        return map_.get(dclass);
    }

    @Override
    public boolean save(Workspace ws, DemetraVersion version, boolean force) {
        if (ws.getDataSource() == null) {
            return false;
        }
        if (!saveWorkspace(ws, version)) {
            return false;
        }
        return ws.getItems().stream()
                .filter(o -> o.isDirty() || (force && !o.getStatus().isVolatile()))
                .noneMatch(o -> !saveItem(o, version));
    }

    protected abstract boolean saveWorkspace(Workspace ws, DemetraVersion version);

    @Override
    public boolean delete(Workspace ws) {
        if (!ws.getItems().stream().noneMatch(o -> !deleteItem(o))) {
            return false;
        }
        return deleteWorkspace(ws);
    }

    protected abstract boolean deleteWorkspace(Workspace ws);

    @Override
    public void close(Workspace ws_) {
    }

    @Override
    public boolean loadItem(WorkspaceItem<?> item) {
        WorkspaceItemManager<?> manager = WorkspaceFactory.getInstance().getManager(item.getFamily());
        if (manager == null) {
            return false;
        }
        List<WorkspaceItemRepository> repos = getRepositories(manager.getItemClass());
        if (repos == null) {
            return false;
        }
        return repos.stream().anyMatch(o -> o.load(item));
    }

    @Override
    public boolean saveItem(WorkspaceItem<?> item, DemetraVersion version) {
        if (!item.getStatus().canBeSaved()) {
            return true;
        }
        List<WorkspaceItemRepository> repos = getRepositories(item);
        if (repos == null) {
            item.resetDirty();
            return true;
        }
        return repos.stream().anyMatch(o -> o.save(item, version));
    }

    @Override
    public boolean deleteItem(WorkspaceItem<?> item) {
        if (!item.getStatus().hasStorage()) {
            return true;
        }
        List<WorkspaceItemRepository> repos = getRepositories(item);
        if (repos == null) {
            return true;
        }
        return repos.stream().anyMatch(o -> o.delete(item));
    }

    @Override
    public <D> boolean canHandleItem(Class<D> dclass) {
        return map_.containsKey(dclass);
    }
}
