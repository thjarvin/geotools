package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.StyleFactory;

public class PointSymbolizerBuilder implements Builder<PointSymbolizer> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    String geometry;

    GraphicBuilder graphic = new GraphicBuilder();

    private boolean unset = false;

    PointSymbolizerBuilder geometry(String geometry) {
        this.geometry = geometry;
        unset = false;
        return this;
    }

    GraphicBuilder graphic() {
        unset = false;
        return graphic;
    }

    public PointSymbolizer build() {
        if( unset ){
            return null;
        }
        PointSymbolizer ps = sf.createPointSymbolizer(graphic.build(), geometry);
        reset();
        return ps;
    }

    public PointSymbolizerBuilder reset() {
        this.geometry = null;
        this.graphic.reset(); // TODO: See what the actual default is
        unset = false;
        
        return this;
    }
    public Builder<PointSymbolizer> reset(PointSymbolizer original) {
        if( original == null ){
            return unset();
        }
        this.geometry = original.getGeometryPropertyName();
        this.graphic.reset( original.getGraphic() );
        unset = false;
        
        return this;
    }
    public PointSymbolizerBuilder unset() {
        this.geometry = null;
        this.graphic.unset();
        unset = true;
        
        return this;
    }
}
