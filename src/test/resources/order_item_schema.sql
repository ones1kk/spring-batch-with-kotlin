create table if not exists order_item (
    order_item_id bigint auto_increment primary key comment '주문 항목 pk',
    order_id bigint not null comment '주문 id',
    product_name varchar(100) not null comment '상품명',
    created_at datetime default current_timestamp comment '생성일'
) engine=innodb;

create index if not exists idx_order_item_order_id
    on order_item (order_id);
