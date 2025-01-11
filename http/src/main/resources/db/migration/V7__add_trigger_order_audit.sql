CREATE OR REPLACE FUNCTION order_audit_trigger_func() RETURNS TRIGGER AS $$
    DECLARE
        newData jsonb;
        oldData jsonb;
        key text;
        newValues jsonb;
        oldValues jsonb;
    BEGIN

        newValues := '{}';
        oldValues := '{}';

        IF TG_OP = 'INSERT' THEN
            newData    := to_jsonb(NEW);
            newValues  := newData;

        ELSIF TG_OP = 'UPDATE' THEN
            newData := to_jsonb(NEW);
            oldData := to_jsonb(OLD);

            FOR key IN SELECT jsonb_object_keys(newData) INTERSECT SELECT jsonb_object_keys(oldData)
                LOOP
                    IF newData ->> key != oldData ->> key THEN
                        newValues := newValues || jsonb_build_object(key, newData ->> key);
                        oldValues := oldValues || jsonb_build_object(key, oldData ->> key);
                    END IF;
                END LOOP;

        ELSIF TG_OP = 'DELETE' THEN
            oldData := to_jsonb(OLD);
            oldValues := oldData;

            FOR key IN SELECT jsonb_object_keys(oldData)
                LOOP
                    oldValues := oldValues || jsonb_build_object(key, oldData ->> key);
                END LOOP;

        END IF;

        IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
            INSERT INTO orders_aud (orderId, operationType, oldValue, newValue)
            VALUES (NEW.orderid, TG_OP, oldValues, newValues);

            RETURN NEW;
        ELSE
            INSERT INTO orders_aud (orderId, operationType, oldValue, newValue)
            VALUES (OLD.orderid, TG_OP, oldValues, newValues);

            RETURN OLD;
        END IF;
    END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER order_audit_trigger
BEFORE INSERT OR UPDATE OR DELETE
ON orders
FOR EACH ROW
EXECUTE FUNCTION order_audit_trigger_func();