package com.eqgis.eqr.bean;

import com.google.android.filament.IndexBuffer;
import com.google.android.filament.VertexBuffer;

/**
 * 图元数据
 * <p>
 *     包含VB和IB数据
 * </p>
 * @author tanyx 2025/12/26
 * @version 1.0
 **/
public class PrimitiveGeometry {
    public final VertexBuffer vertexBuffer;
    public final IndexBuffer indexBuffer;

    PrimitiveGeometry(VertexBuffer vb, IndexBuffer ib) {
        this.vertexBuffer = vb;
        this.indexBuffer = ib;
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }
}
