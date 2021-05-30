package games.rednblack.editor.data.migrations;

import games.rednblack.h2d.common.vo.ProjectVO;

public interface IVersionMigrator {
    void setProject(String path, ProjectVO vo);
    boolean doMigration();
}
