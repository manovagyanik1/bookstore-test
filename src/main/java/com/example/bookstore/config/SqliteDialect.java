package com.example.bookstore.config;

import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.dialect.LockClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.data.relational.core.sql.LockOptions;

public class SqliteDialect extends AbstractDialect implements JdbcDialect {
    
    private static final LimitClause LIMIT_CLAUSE = new LimitClause() {
        @Override
        public String getLimit(long limit) {
            return "LIMIT " + limit;
        }

        @Override
        public String getOffset(long offset) {
            return "OFFSET " + offset;
        }

        @Override
        public String getLimitOffset(long limit, long offset) {
            return String.format("LIMIT %d OFFSET %d", limit, offset);
        }

        @Override
        public LimitClause.Position getClausePosition() {
            return LimitClause.Position.AFTER_ORDER_BY;
        }
    };
    
    private static final LockClause LOCK_CLAUSE = new LockClause() {
        @Override
        public String getLock(LockOptions lockOptions) {
            return "";
        }

        @Override
        public LockClause.Position getClausePosition() {
            return LockClause.Position.AFTER_FROM_TABLE;
        }
    };
    
    @Override
    public LimitClause limit() {
        return LIMIT_CLAUSE;
    }

    @Override
    public LockClause lock() {
        return LOCK_CLAUSE;
    }

    @Override
    public org.springframework.data.jdbc.core.dialect.JdbcArrayColumns getArraySupport() {
        return org.springframework.data.jdbc.core.dialect.JdbcArrayColumns.Unsupported.INSTANCE;
    }

    @Override
    public IdentifierProcessing getIdentifierProcessing() {
        return IdentifierProcessing.ANSI;
    }
}
