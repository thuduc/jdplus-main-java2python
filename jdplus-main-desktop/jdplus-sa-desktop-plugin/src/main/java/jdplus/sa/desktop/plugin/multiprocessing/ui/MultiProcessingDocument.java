/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.desktop.plugin.multiprocessing.ui;

import java.time.Clock;
import java.time.LocalDateTime;
import jdplus.sa.base.api.EstimationPolicy;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaItem;
import jdplus.sa.base.api.SaItems;
import jdplus.sa.base.api.SaSpecification;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.util.Documented;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.toolkit.base.tsp.TsMeta;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
public class MultiProcessingDocument implements Documented {

    int curId = 0;

    private final Map<String, String> metadata = new LinkedHashMap<>();

    private final List<SaNode> current = new ArrayList<>();
    @NonNull
    private final SaItems initial;

    private MultiProcessingDocument(SaItems initial) {
        this.initial = initial;
        this.current.addAll(of(initial.getItems()));
    }

    private List<SaNode> of(List<SaItem> items) {
        return items.stream().map(item -> SaNode.of(curId++, item.copy())).collect(Collectors.<SaNode>toList());
    }

    public List<SaNode> getCurrent() {
        return current;
    }

    public SaItems getInitial() {
        return initial;
    }

    public static MultiProcessingDocument createNew() {
        MultiProcessingDocument doc = new MultiProcessingDocument(SaItems.empty());
        return doc;
    }

    public static MultiProcessingDocument open(SaItems initial) {
        MultiProcessingDocument doc = new MultiProcessingDocument(initial);
        return doc;
    }

    public MultiProcessingDocument save() {
        Map<String, String> meta = new HashMap<>(metadata);
        SaItems cur = current(meta);
        MultiProcessingDocument saved = open(cur);
        saved.metadata.putAll(metadata);
        TsMeta.TIMESTAMP.store(saved.metadata, LocalDateTime.now(Clock.systemDefaultZone()));
        return saved;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public boolean hasFrozenItems() {
        Optional<SaNode> any = current.stream().filter(o -> o.getOutput().getDefinition().getTs().isFrozen()).findAny();
        return any.isPresent();
    }

    public boolean isProcessed() {
        Optional<SaNode> any = current.stream().filter(o -> !o.isProcessed()).findAny();
        return any.isEmpty();
    }

    public boolean isNew() {
        return initial.isEmpty();
    }

    public boolean isRefreshable() {
        return !isNew() || hasFrozenItems();
    }

    public boolean isNew(SaNode node) {
        return node.getId() >= initial.size();
    }

    public void refresh(EstimationPolicy policy) {
        current.clear();
        current.addAll(of(initial.refresh(policy, TsInformationType.Data)));
    }

    public void refresh(EstimationPolicy policy, Predicate<SaNode> test) {
        for (int i = 0; i < current.size(); ++i) {
            SaNode cur = current.get(i);
            int id = cur.getId();
            if (test.test(cur)) {
                SaItem item = null;
                if (id < initial.size()) {
                    item = initial.item(id);
                } else if (cur.getOutput().getDefinition().getTs().isFrozen()) {
                    item = cur.getOutput();
                }
                if (item != null) {
                    SaNode n = SaNode.of(id, item.refresh(policy, TsInformationType.Data));
                    current.set(i, n);
                }
            }
        }
    }

    public void refresh(EstimationPolicyType policy, TimeSelector span, Predicate<SaNode> test) {
        for (int i = 0; i < current.size(); ++i) {
            SaNode cur = current.get(i);
            int id = cur.getId();
            if (test.test(cur)) {
                SaItem item = null;
                if (id < initial.size()) {
                    item = initial.item(id);
                } else if (cur.getOutput().getDefinition().getTs().isFrozen()) {
                    item = cur.getOutput();
                }
                if (item != null) {
                    Ts ts = item.getDefinition().getTs();
                    TsDomain domain = ts.getData().getDomain().select(span);
                    SaNode n = SaNode.of(id, item.refresh(new EstimationPolicy(policy, domain), TsInformationType.Data));
                    current.set(i, n);
                }
            }
        }
    }

    public void refresh(EstimationPolicyType policy, int nback, Predicate<SaNode> test) {
        for (int i = 0; i < current.size(); ++i) {
            SaNode cur = current.get(i);
            int id = cur.getId();
            if (test.test(cur)) {
                SaItem item = null;
                if (id < initial.size()) {
                    item = initial.item(id);
                } else if (cur.getOutput().getDefinition().getTs().isFrozen()) {
                    item = cur.getOutput();
                }
                if (item != null) {
                    TsDomain domain = null;
                    if (nback != 0) {
                        Ts ts = item.getDefinition().getTs();
                        domain = ts.getData().getDomain();
                        if (nback < 0) {
                            nback = -nback * domain.getAnnualFrequency();
                        }
                        domain = domain.drop(0, nback);
                    }
                    SaNode n = SaNode.of(id, item.refresh(new EstimationPolicy(policy, domain), cur.domainSpec(), TsInformationType.Data));
                    current.set(i, n);
                }
            }
        }
    }

    public SaItems current(Map<String, String> nmeta) {

        current.forEach(cur -> cur.prepare());

        return SaItems.builder()
                .meta(nmeta)
                .items(current.stream().map(node -> node.getOutput()).collect(Collectors.toList()))
                .build();
    }

    public void add(@NonNull SaSpecification spec, Ts... nts) {

        for (Ts ts : nts) {
            current.add(SaNode.of(++curId, ts, spec));
        }
    }

    public void add(SaItem... nitems) {
        for (SaItem item : nitems) {
            current.add(SaNode.of(++curId, item));
        }
    }

    public SaNode search(int id) {
        Optional<SaNode> found = current.stream().filter(node -> node.getId() == id).findFirst();
        return found.isPresent() ? found.orElseThrow() : null;
    }

    public void replace(int id, SaItem nitem) {
        SaNode node = search(id);
        if (node != null) {
            node.setOutput(nitem);
        }
    }

    public int positionOfId(int id) {
        for (int i = 0; i < current.size(); ++i) {
            if (id == current.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    public void removeId(int id) {
        int pos = positionOfId(id);
        if (pos >= 0) {
            current.remove(pos);
        }
    }

    public void replace(int id, SaNode nitem) {
        int pos = positionOfId(id);
        current.set(pos, nitem);
    }

    public void remove(int pos) {
        current.remove(pos);
    }

    public void remove(Collection<SaNode> nodes) {
        current.removeAll(nodes);
    }

    public void removeAll() {
        current.clear();
    }

    public void restore() {
        this.current.clear();
        this.curId = 0;
        current.addAll(of(initial.getItems()));
    }

    public SaItem[] all() {
        return current.stream().peek(o -> o.prepare()).map(o -> o.getOutput()).toArray(n -> new SaItem[n]);
    }
}
