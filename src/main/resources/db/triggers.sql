-- Función para actualizar el stock automáticamente al insertar un detalle de pedido
CREATE OR REPLACE FUNCTION actualizar_stock_producto()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE productos
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualización de stock
CREATE TRIGGER tr_actualizar_stock
AFTER INSERT ON detalles_pedido
FOR EACH ROW
EXECUTE FUNCTION actualizar_stock_producto();

-- Tabla de auditoría para cambios de precios
CREATE TABLE IF NOT EXISTS auditoria_precios (
    id SERIAL PRIMARY KEY,
    producto_id INT NOT NULL,
    precio_anterior DECIMAL(19,2),
    precio_nuevo DECIMAL(19,2),
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_cambio VARCHAR(255)
);

-- Función para auditar cambios de precios
CREATE OR REPLACE FUNCTION auditar_cambio_precio()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.precio <> NEW.precio THEN
        INSERT INTO auditoria_precios(producto_id, precio_anterior, precio_nuevo, fecha_cambio)
        VALUES (OLD.id, OLD.precio, NEW.precio, CURRENT_TIMESTAMP);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para auditoría de precios
CREATE TRIGGER tr_auditoria_precios
AFTER UPDATE ON productos
FOR EACH ROW
EXECUTE FUNCTION auditar_cambio_precio();
