/*
 * Copyright (C) 2016 favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package storybook.exim.importer;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import storybook.db.abs.AbstractEntity;
import storybook.db.attribute.Attribute;
import storybook.db.attribute.Attributes;
import storybook.db.book.Book;
import storybook.db.category.Category;
import storybook.db.chapter.Chapter;
import storybook.db.event.Event;
import storybook.db.gender.Gender;
import storybook.db.idea.Idea;
import storybook.db.item.Item;
import storybook.db.location.Location;
import storybook.db.memo.Memo;
import storybook.db.part.Part;
import storybook.db.person.Person;
import storybook.db.relation.Relation;
import storybook.db.scene.Scene;
import storybook.db.strand.Strand;
import storybook.db.tag.Tag;
import storybook.project.Project;
import storybook.tools.LOG;
import storybook.tools.xml.XmlUtil;
import storybook.ui.MainFrame;

/**
 *
 * @author favdb
 */
public class ImportUtil {

    private ImportUtil() {
        throw new IllegalStateException("Utility class");
    }

    static List<ImportEntity> list(Project session, Importer imp, String tobj) {
        //App.printInfos("ImportEntities.list("+imp.fileName+","+tobj+")");
        if (!imp.isOpened()) {
            LOG.err("error Importer is not opened");
            return (new ArrayList<>());
        }
        if (imp.isXml()) {
            return (listXml(imp, tobj));
        }
        return (listH2(session, imp, tobj));
    }

    static List<ImportEntity> listXml(Importer imp, String tobj) {
        //App.printInfos("ImportEntities.Xml("+imp.fileName+","+tobj+")");
        List<ImportEntity> entities = new ArrayList<>();
        NodeList nodes = imp.rootNode.getElementsByTagName(tobj);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (!n.getParentNode().equals(imp.rootNode)) {
                continue;
            }
            switch (Book.getTYPE(tobj)) {
                case STRAND:
                    Strand strand = Strand.fromXml(n);
                    if (strand.getId() > 1) {
                        entities.add(new ImportEntity(strand, n));
                    }
                    break;
                case PART:
                    Part part = Part.fromXml(n);
                    if (part.getId() > 1) {
                        entities.add(new ImportEntity(part, n));
                    }
                    break;
                case CHAPTER:
                    entities.add(new ImportEntity(Chapter.fromXml(n), n));
                    break;
                case SCENE:
                    entities.add(new ImportEntity(Scene.fromXml(n), n));
                    break;
                case PERSON:
                    entities.add(new ImportEntity(Person.fromXml(n), n));
                    break;
                case GENDER:
                    Gender gender = Gender.fromXml(n);
                    //import only other than male female
                    if (gender.getId() > 2) {
                        entities.add(new ImportEntity(gender, n));
                    }
                    break;
                case CATEGORY:
                    Category category = Category.fromXml(n);
                    //import only other than central and secondary characters
                    if (category.getId() > 2) {
                        entities.add(new ImportEntity(category, n));
                    }
                    break;
                case LOCATION:
                    entities.add(new ImportEntity(Location.fromXml(n), n));
                    break;
                case ITEM:
                    entities.add(new ImportEntity(Item.fromXml(n), n));
                    break;
                case TAG:
                    entities.add(new ImportEntity(Tag.fromXml(n), n));
                    break;
                case EVENT:
                    entities.add(new ImportEntity(Event.fromXml(n), n));
                    break;
                default:
                    break;
            }
        }
        return (entities);
    }

    @SuppressWarnings("unchecked")
    static List<ImportEntity> listH2(Project session, Importer imp, String tobj) {
        List<ImportEntity> ientities = new ArrayList<>();
        List<AbstractEntity> entities = null;
        switch (Book.getTYPE(tobj)) {
            case STRAND:
                entities = session.getList(Book.TYPE.STRAND);
                break;
            case PART:
                entities = session.getList(Book.TYPE.PART);
                break;
            case CHAPTER:
                entities = session.getList(Book.TYPE.CHAPTER);
                break;
            case SCENE:
                entities = session.getList(Book.TYPE.SCENE);
                break;
            case PERSON:
                entities = session.getList(Book.TYPE.PERSON);
                break;
            case CATEGORY:
                entities = session.getList(Book.TYPE.CATEGORY);
                break;
            case GENDER:
                entities = session.getList(Book.TYPE.GENDER);
                break;
            case LOCATION:
                entities = session.getList(Book.TYPE.LOCATION);
                break;
            case ITEM:
                entities = session.getList(Book.TYPE.ITEM);
                break;
            case TAG:
                entities = session.getList(Book.TYPE.TAG);
                break;
            case IDEA:
                entities = session.getList(Book.TYPE.IDEA);
                break;
            case MEMO:
                entities = session.getList(Book.TYPE.MEMO);
                break;
            case RELATION:
                entities = session.getList(Book.TYPE.RELATION);
                break;
            case EVENT:
                entities = session.getList(Book.TYPE.EVENT);
                break;
            default:
                break;
        }
        if (entities != null) {
            for (AbstractEntity entity : entities) {
                ientities.add(new ImportEntity(entity, null));
            }
        }
        return ientities;
    }

    static AbstractEntity updateEntity(MainFrame mainFrame, ImportEntity newEntity, AbstractEntity oldEntity) {
        /*App.printInfos("ImportUtil.updateEntity("+"mainFrame,"+
			EntityUtil.getClassName(newEntity.entity) +","+
			EntityUtil.getClassName(oldEntity)+")");*/
        if (oldEntity != null) {
            newEntity.entity.setId(oldEntity.getId());
        } else {
            newEntity.entity.setId(-1L);
        }
        switch (Book.getTYPE(newEntity.entity)) {
            case STRAND:
                return (updateStrand(mainFrame, newEntity, (Strand) oldEntity));
            case PART:
                return (updatePart(mainFrame, newEntity, (Part) oldEntity));
            case CHAPTER:
                return (updateChapter(mainFrame, newEntity, (Chapter) oldEntity));
            case SCENE:
                return (updateScene(mainFrame, newEntity, (Scene) oldEntity));
            case CATEGORY:
                return (updateCategory(mainFrame, newEntity, (Category) oldEntity));
            case GENDER:
                return (updateGender(mainFrame, newEntity, (Gender) oldEntity));
            case PERSON:
                return (updatePerson(mainFrame, newEntity, (Person) oldEntity));
            case LOCATION:
                return (updateLocation(mainFrame, newEntity, (Location) oldEntity));
            case ITEM:
                return (updateItem(mainFrame, newEntity, (Item) oldEntity));
            case TAG:
                return (updateTag(mainFrame, newEntity, (Tag) oldEntity));
            case IDEA:
                return (updateIdea(mainFrame, newEntity, (Idea) oldEntity));
            case MEMO:
                return (updateMemo(mainFrame, newEntity, (Memo) oldEntity));
            case RELATION:
                return (updateRelation(mainFrame, newEntity, (Relation) oldEntity));
            case EVENT:
                return (updateEvent(mainFrame, newEntity, (Event) oldEntity));
            default:
                AbstractEntity en = newEntity.entity;
                LOG.err("Unknown object type \""
                        + (en == null ? "null" : en.getClass().getSimpleName().toLowerCase())
                        + "\" do nothing");
                return newEntity.entity;

        }
    }

    private static AbstractEntity updateStrand(MainFrame mainFrame,
            ImportEntity newEntity, Strand oldEntity) {
        //LOG.printInfos(TT+".updateStrand(...)");
        // as is, no link to update
        Strand entity = (Strand) newEntity.entity;
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updatePart(MainFrame mainFrame,
            ImportEntity newEntity, Part oldEntity) {
        //App.printInfos("ImportUtil.updatePart(...)");
        // creationTime, doneTime, objectiveTime, objectiveChars not modified
        Part entity = (Part) newEntity.entity;
        if (oldEntity != null) {
            entity.setCreationTime(oldEntity.getCreationTime());
            entity.setDoneTime(oldEntity.getDoneTime());
            entity.setObjectiveChars(oldEntity.getObjectiveChars());
            entity.setObjectiveTime(oldEntity.getObjectiveTime());
            if (oldEntity.hasSuperpart()) {
                entity.setSuperpart(oldEntity.getSuperpart());
            }
        }
        if (newEntity.node != null) {
            String str = XmlUtil.getText(newEntity.node, "superpart");
            if (!str.isEmpty()) {
                entity.setSuperpart((Part) mainFrame.project.parts.findName(str));
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateChapter(MainFrame mainFrame, ImportEntity newEntity, Chapter oldEntity) {
        //App.printInfos("ImportUtil.updateChapter(...)");
        Chapter entity = (Chapter) newEntity.entity;
        // creationTime, doneTime, objectiveTime, objectiveChars not modified
        if (oldEntity != null) {
            entity.setCreationTime(oldEntity.getCreationTime());
            entity.setDoneTime(oldEntity.getDoneTime());
            entity.setObjectiveChars(oldEntity.getObjectiveChars());
            entity.setObjectiveTime(oldEntity.getObjectiveTime());
        }
        if (newEntity.node != null) {
            String str = XmlUtil.getText(newEntity.node, "part");
            if (!str.isEmpty()) {
                entity.setPart((Part) mainFrame.project.parts.findName(str));
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateScene(MainFrame mainFrame, ImportEntity newEntity, Scene oldEntity) {
        //App.printInfos("ImportUtil.updateScene(...)");
        Scene entity = (Scene) newEntity.entity;
        if (oldEntity != null) {
            entity.setStrand(oldEntity.getStrand());
            entity.setChapter(oldEntity.getChapter());
            entity.setPersons(oldEntity.getPersons());
            entity.setItems(oldEntity.getItems());
        }
        if (newEntity.node != null) {
            String str = XmlUtil.getText(newEntity.node, "strand");
            if (!str.isEmpty()) {
                entity.setStrand((Strand) mainFrame.project.strands.findName(str));
            }
            str = XmlUtil.getText(newEntity.node, "chapter");
            if (!str.isEmpty()) {
                entity.setChapter((Chapter) mainFrame.project.chapters.findName(str));
            }
            List<String> list;
            list = XmlUtil.getList(newEntity.node, "strands");
            if (!list.isEmpty()) {
                List<Strand> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Strand) mainFrame.project.strands.findName(xstr));
                }
                entity.setStrands(lstEntity);
            }
            list = XmlUtil.getList(newEntity.node, "persons");
            if (!list.isEmpty()) {
                List<Person> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Person) mainFrame.project.persons.findName(xstr));
                }
                entity.setPersons(lstEntity);
            }
            list = XmlUtil.getList(newEntity.node, "locations");
            if (!list.isEmpty()) {
                List<Location> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Location) mainFrame.project.locations.findName(xstr));
                }
                entity.setLocations(lstEntity);
            }
            list = XmlUtil.getList(newEntity.node, "items");
            if (!list.isEmpty()) {
                List<Item> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Item) mainFrame.project.items.findName(xstr));
                }
                entity.setItems(lstEntity);
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateCategory(MainFrame mainFrame, ImportEntity newEntity, Category oldEntity) {
        //App.printInfos("ImportUtil.updateCategory(...)");
        Category entity = (Category) newEntity.entity;
        if (oldEntity != null) {
            entity.setSup(oldEntity.getSup());
        }
        if (newEntity.node != null) {
            String str = XmlUtil.getText(newEntity.node, "sup");
            if (!str.isEmpty()) {
                entity.setSup((Category) mainFrame.project.categorys.findName(str));
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateGender(MainFrame mainFrame, ImportEntity newEntity, Gender oldEntity) {
        //App.printInfos("ImportUtil.updateGender(...)");
        Gender entity = (Gender) newEntity.entity;
        // as is, no link to update
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updatePerson(MainFrame mainFrame, ImportEntity newEntity, Person oldEntity) {
        //App.printInfos("ImportUtil.updatePerson(...)");
        Person entity = (Person) newEntity.entity;
        // gender, category, attributes not modified
        if (oldEntity != null) {
            entity.setGender(oldEntity.getGender());
            entity.setCategory(oldEntity.getCategory());
            entity.setAttributes(oldEntity.getAttributes());
        }
        if (newEntity.node != null) {
            String str;
            str = XmlUtil.getText(newEntity.node, "gender");
            if (!str.isEmpty()) {
                entity.setGender((Gender) mainFrame.project.genders.findName(str));
            }
            str = XmlUtil.getText(newEntity.node, "category");
            if (!str.isEmpty()) {
                entity.setCategory((Category) mainFrame.project.categorys.findName(str));
            }
            List<String> list;
            list = XmlUtil.getList(newEntity.node, "attribute");
            if (!list.isEmpty()) {
                List<Attribute> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    String key, val;
                    key = xstr.substring(1, xstr.indexOf("]"));
                    val = xstr.substring(xstr.indexOf("]") + 1);
                    Attribute attribute = new Attribute(key, val);
                    Attribute findEntity = Attributes.find(mainFrame, key, val);
                    if (findEntity == null) {
                        mainFrame.project.write(attribute);
                        findEntity = Attributes.find(mainFrame, key, val);
                        if (findEntity != null) {
                            lstEntity.add(findEntity);
                        }
                    } else {
                        lstEntity.add(findEntity);
                    }
                }
                entity.setAttributes(lstEntity);
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateLocation(MainFrame mainFrame, ImportEntity newEntity, Location oldEntity) {
        //App.printInfos("ImportUtil.updateLocation(...)");
        Location entity = (Location) newEntity.entity;
        if (oldEntity != null) {
            entity.setSite(oldEntity.getSite());
        }
        if (newEntity.node != null) {
            String str;
            str = XmlUtil.getText(newEntity.node, "site");
            if (!str.isEmpty()) {
                entity.setSite((Location) mainFrame.project.locations.findName(str));
            }
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateItem(MainFrame mainFrame,
            ImportEntity newEntity, Item oldEntity) {
        //App.printInfos("ImportUtil.updateItem(...)");
        Item entity = (Item) newEntity.entity;
        if (oldEntity != null) {
            entity.setType(oldEntity.getType());
        }
        if (newEntity.node != null) {
            //update nothing
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateTag(MainFrame mainFrame, ImportEntity newEntity, Tag oldEntity) {
        //App.printInfos("ImportUtil.updateTag(...)");
        Tag entity = (Tag) newEntity.entity;
        if (oldEntity != null) {
            entity.setType(oldEntity.getType());
        }
        if (newEntity.node != null) {
            //update nothing
        }
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateIdea(MainFrame mainFrame, ImportEntity newEntity, Idea oldEntity) {
        //App.printInfos("ImportUtil.updateIdea(...)");
        Idea entity = (Idea) newEntity.entity;
        // nothing to change
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateMemo(MainFrame mainFrame, ImportEntity newEntity, Memo oldEntity) {
        //App.printInfos("ImportUtil.updateMemo(...)");
        Memo entity = (Memo) newEntity.entity;
        // nothing to change
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateEvent(MainFrame mainFrame, ImportEntity newEntity, Event oldEntity) {
        //App.printInfos(TT+".updateEvent(...)");
        Event entity = (Event) newEntity.entity;
        // nothing to change
        return ((AbstractEntity) entity);
    }

    private static AbstractEntity updateRelation(MainFrame mainFrame, ImportEntity newEntity, Relation oldEntity) {
        //App.printInfos(TT+".updateRelation(...)");
        Relation entity = (Relation) newEntity.entity;
        if (oldEntity != null) {
        }
        if (newEntity.node != null) {
            List<String> list;
            list = XmlUtil.getList(newEntity.node, "persons");
            if (!list.isEmpty()) {
                List<Person> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Person) mainFrame.project.persons.findName(xstr));
                }
                entity.setPersons(lstEntity);
            }
            list = XmlUtil.getList(newEntity.node, "locations");
            if (!list.isEmpty()) {
                List<Location> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Location) mainFrame.project.locations.findName(xstr));
                }
                entity.setLocations(lstEntity);
            }
            list = XmlUtil.getList(newEntity.node, "items");
            if (!list.isEmpty()) {
                List<Item> lstEntity = new ArrayList<>();
                for (String xstr : list) {
                    lstEntity.add((Item) mainFrame.project.items.findName(xstr));
                }
                entity.setItems(lstEntity);
            }
        }
        return ((AbstractEntity) entity);
    }

}
