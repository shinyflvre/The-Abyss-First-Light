package net.yezon.theabyss.fabric.core;

import java.util.Map;
import net.yezon.theabyss.platform.attachment.AttachmentType;

public interface AttachmentStorage {
    Map<AttachmentType<?>, Object> theabyss$attachments();
}
